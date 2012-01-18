#!/usr/bin/env groovy
/*
 * Copyright 2012 DTO Solutions, Inc. (http://dtosolutions.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/*
* Author: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 1/16/2012
* edam.groovy - a gouda-like wrapper for pandoc in groovy
* inspired by gouda: http://www.unexpected-vortices.com/sw/gouda/docs/
*/

pdocextra=[]
help=false
copyOriginal=false
doToc=true
doNav=true
tocAsIndex=true
autoFiles=new HashSet()
tempFiles=new HashSet()
cleanUpAuto=true
verbose=false
optionsDefaults=[
        chapterNumbers:'true',
        chapterTitle: 'Chapter',
        pageFileName:'${id}',
        cssFileName:'style.css',
        indexFileName:'(index|readme)',
        indexFileOutputName:'index',
        tocFileName:'toc',
        tocTitle:'Table of Contents']
optionDescs=[
    chapterNumbers:'True/false, use chapter numbers in navigation.',
    chapterTitle:'Localized text to use for a chapter title.',
    pageFileName: 'Template for generated filename for each page, variables: `${index}`,`${id}`.',
    cssFileName:'File name of the css file to link in the HTML header.',
    indexFileName: 'File name base expected as index markdown file.',
    indexFileOutputName: 'File name base used as index HTML file.',
    tocFileName: 'File name expected/generated as table-of-contents file.',
    tocTitle: 'Localized text to use for table of contents title.'
    ]
    
options = new HashMap(optionsDefaults)

def readTitle(File file){
    def count=0
    def title=null
    if(file && file.exists()){
        file.withReader { reader ->
            def match=reader.readLine()=~/^%\s+(.+)$/
            if(match.matches()){
                title=match.group(1)
            }
        }
    }
    return title
}
def titleToIdentifier(String title){
    def str=title
    str= str.replaceAll(/['":\/<>,;\[\]\{\}\|\\!@#\$%\^&\*\(\)\+~`\?=]/,'')
    str= str.replaceAll(/[\s]/,'-')
    str = str.toLowerCase()
    str = str.replaceAll(/^[^a-z]+/,'')
    
    return str
}
def readIndexFile(File dir){
    def nameRegex="^"+options.indexFileName+'\\.(md|txt)$'
    def file=dir.listFiles().find{it.name=~nameRegex}
    def index=[:]
    def title=readTitle(file)
    if(title){
        def outfile="${options.indexFileOutputName}.html"
        index.title=title
        index.index=-1
        index.outfile=outfile
        index.file=file
    }else{
        //no index
    }
    return index
}
def replaceParams(String templ,Map params){
    def replaced=templ.replaceAll(/(\$\{(.+?)\})/,{all,match1,match2->
            null!=params[match2]?params[match2]:match1
        })
    return replaced
}
def readToc(File dir){
    def tocfile=new File(dir,"toc.conf")
    def toc=null
    if(tocfile.exists()){    
        toc=[]
        tocfile.withReader { reader ->
            def line=reader.readLine()
            while(null!=line){
                def match=line=~/^(\d+):([^:]+):(.+)$/
                if(match.matches()){
                    def file=new File(dir,match.group(2))
                    def ndx=match.group(1).toInteger()
                    def title=match.group(3).trim()
                    def outfile
                    def identifier=titleToIdentifier(title)
                    def params=[id:identifier,index:ndx]
                    def filestub=replaceParams(options.pageFileName,params)
                    outfile=filestub+".html"
                    toc<<[
                        index:ndx,
                        file:file,
                        title:title,
                        outfile:outfile
                        ]
                }
                line=reader.readLine()
            }
        }
    }
    return toc
}
def getToc(File dir){
    def nameRegex='^(toc|'+options.indexFileName+')\\.(md|txt)$'
    def mdfiles=dir.listFiles().findAll{(it.name=~/\.(md|txt)$/) && !(it.name=~nameRegex)}
    def tocfile=new File(dir,"toc.conf")
    def readtoc=readToc(dir)
    def toc=[]
    if(!tocfile.exists()){
        autoFiles<<tocfile
        //create toc.conf
        def ndx=1
        tocfile.withWriter { out ->
            mdfiles.each{file->
                def title=readTitle(file)
                def outfile
                def identifier=titleToIdentifier(title)
                def params=[id:identifier,index:ndx]
                def filestub=replaceParams(options.pageFileName,params)
                outfile=filestub+".html"
                if(title){
                    toc<<[index:ndx,file:file,title:title,outfile:outfile]
                    out.writeLine("${ndx}:${file.name}:${title}")
                    ndx+=1
                }else{
                    println "Warning: File has no title: ${file}"
                }
            }
        }
    }else{
        //compare read to mdfiles
        def common=readtoc.collect{it.file}.intersect(mdfiles)
        if(common.size()!=mdfiles.size()){
            println "Warning: some files changed compared to the TOC, please update toc.txt or remove it to regenerate: ${common}"
            return null
        }
        
        toc=readtoc
    }
    return toc
}

def createTocMdFile(File dir,toc,title,content=null){
    def tocout=new File(dir,"toc.txt")
    if(!tocout.exists()){
        autoFiles<<tocout
    }
    tocout.withWriter{writer->
        
        if(content){
            writer<<content
            writer<<"\n\n"
        }else{
            writer<<"% ${title}\n\n"
        }
        
        writer<<"## ${options.tocTitle}\n\n"
        toc.each{titem->
            writer<<"${titem.index}. [${titem.title}](${titem.outfile})\n"
        }
    }
}
defaultTemplates=[before:'''<div id="docbody">
''',after:'''</div>
''',html:'''<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>$if(title-prefix)$$title-prefix$ - $endif$$if(pagetitle)$$pagetitle$$endif$</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta name="generator" content="pandoc" />
$for(author)$
  <meta name="author" content="$author$" />
$endfor$
$if(date)$
  <meta name="date" content="$date$" />
$endif$
$if(highlighting-css)$
  <style type="text/css">
$highlighting-css$
  </style>
$endif$
$for(css)$
  <link rel="stylesheet" href="$css$" type="text/css" />
$endfor$
$if(math)$
  $math$
$endif$
$for(header-includes)$
  $header-includes$
$endfor$
</head>
<body>
$for(include-before)$
$include-before$
$endfor$
$if(title)$
<h1 class="title">$title$</h1>
$endif$
$if(toc)$
$toc$
$endif$
$body$
$for(include-after)$
$include-after$
$endfor$
</body>
</html>''',navToc:'''<nav class="page $navclass$">
    <ul>
        <li class="current"><a href="$currentpagelink$">$currentpage$</a></li>
        <li class="next"><a href="$nextpagelink$">$nextpage$</a></li>
    </ul>
</nav>'''    ,navFirst:'''<nav class="page $navclass$">
    <ul>
        <li class="current"><a href="$currentpagelink$">$currentpage$</a></li>
        <li class="toc"><a href="$tocpagelink$">$tocpage$</a></li>
        <li class="next"><a href="$nextpagelink$">$nextpage$</a></li>
    </ul>
</nav>'''    ,nav:'''<nav class="page $navclass$">
    <ul>
        <li class="current"><a href="$currentpagelink$">$currentpage$</a></li>
        <li class="toc"><a href="$tocpagelink$">$tocpage$</a></li>
        <li class="prev"><a href="$prevpagelink$">$prevpage$</a></li>
        <li class="next"><a href="$nextpagelink$">$nextpage$</a></li>
    </ul>
</nav>'''    ,navLast:'''<nav class="page $navclass$">
    <ul>
        <li class="current"><a href="$currentpagelink$">$currentpage$</a></li>
        <li class="toc"><a href="$tocpagelink$">$tocpage$</a></li>
        <li class="prev"><a href="$prevpagelink$">$prevpage$</a></li>
    </ul>
</nav>''']

def getTemplates(File tdir){
    def templates=[:]
    defaultTemplates.keySet().each{k->
        templates[k]=new File(tdir,"${k}.template")
    }
    templates.each{k,v->
        if(!v.exists()){
            autoFiles<<v
            if(!v.parentFile.exists()){
                autoFiles<<v.parentFile
            }
            v.parentFile.mkdirs()
            v.withWriter{w->w.write(defaultTemplates[k])}
        }
    }
    return templates
}

pandocCmd="pandoc"
def runPandoc(List longparams,file){
    def panargs = [] 
    panargs << pandocCmd
    panargs.addAll longparams
    panargs.add file
    if(verbose){
        println "Run: ${panargs.join(' ')}"
    }
    return panargs.execute()
}

def generateAll(toc,templates,File dir, File outdir){
    def ndxtoc=[    ]
    if(doToc){
        ndxtoc<<[index:0,title:options.tocTitle,file:new File(dir,'toc.txt'),outfile:tocAsIndex?"${options.indexFileOutputName}.html":'toc.html']
    }
    def index=readIndexFile(dir)
    if(index && !tocAsIndex){
        ndxtoc = [index]+ndxtoc
    }else if(index && tocAsIndex && doToc){
        ndxtoc[0].title=index.title
    }
    if(doToc){
        createTocMdFile(dir,toc,ndxtoc.find{0==it.index}.title,tocAsIndex?index.file.text:null)
    }
    def allpages=ndxtoc + toc
    def navfile=new File(dir,'temp-nav-top.html')
    def navfileB=new File(dir,'temp-nav-bot.html')
    if(1==allpages.size()){
        doNav=false
    }else{
        allpages.find{0==it.index}?.put('nav',templates.navToc)
        allpages[-1].nav=allpages[-1].nav?:templates.navLast
        if(allpages.size()>2){
            allpages.find{1==it.index}?.put('nav',templates.navFirst)
        }
    }
    
    tempFiles<<navfile
    tempFiles<<navfileB
    allpages.eachWithIndex{titem,x->
        def pargs=['-B',templates.before]
        if(doNav){
            //def tnav=0==titem.index?templates.navToc:x<2?templates.navFirst:x<allpages.size()-1?templates.nav:templates.navLast
            def tnav=titem.nav?:templates.nav
            
            def navcontent=tnav.text
            def navcontentB=navcontent
            //set up nav links
            def navs=[currentpage:x>1 && options.chapterNumbers=='true' ?"${options.chapterTitle} ${titem.index}: ${titem.title}":titem.title,currentpagelink:titem.outfile]
            if(x<allpages.size()-1){
                //first page
                navs.nextpage=x+1>1 && options.chapterNumbers=='true'?"${options.chapterTitle} ${allpages[x+1].index}: ${allpages[x+1].title}":allpages[x+1].title
                navs.nextpagelink=allpages[x+1].outfile
            }
            if(x>0){
                //last page
                navs.prevpage=x-1>1 && options.chapterNumbers=='true'?"${options.chapterTitle} ${allpages[x-1].index}: ${allpages[x-1].title}":allpages[x-1].title
                navs.prevpagelink=allpages[x-1].outfile
            }
            if(doToc){
                navs.tocpage=options.tocTitle
                navs.tocpagelink=tocAsIndex?"${options.indexFileOutputName}.html":'toc.html'
            }
            navs.navclass='top'
            navs.keySet().each{k->
                navcontent=navcontent.replaceAll(/\$${k}\$/,navs[k])
            }
            navs.navclass='bottom'
            navs.keySet().each{k->
                navcontentB=navcontentB.replaceAll(/\$${k}\$/,navs[k])
            }
            navfile.withWriter{writer->
                writer.write(navcontent)
            }
            navfileB.withWriter{writer->
                writer.write(navcontentB)
            }
            pargs.addAll(['-B',navfile,'-A',navfileB])
        }
        pargs.addAll(['-A',templates.after,'-o',new File(outdir,titem.outfile).absolutePath,'-s',"--css=${options.cssFileName}"])
        if(titem.index>0){
            pargs<<"--toc"
        }
        def proc=runPandoc(pargs,titem.file)
        proc.consumeProcessOutput(System.out,System.err)
        def result=proc.waitFor()
        if(0!=result){
            println "Error running pandoc, result: ${result}"
            return
        }
        if(titem.index>0 && copyOriginal && outdir!=dir){
            //copy original file to output dir
            def copy=new File(outdir,titem.file.name)
            if(copy!=titem.file){
                copy.withWriter{w->
                    w.write(titem.file.text)
                }
            }
        }
    }
}
def docsdir
def tdir
def outputdir
def x=0
while(x<args.length){
    switch(args[x]){
        case '-h':
            help=true
            break
        case '--help':
            help=true
            break
        case '-d':
            docsdir=new File(args[x+1])
            x++
            break
        case '-t':
            tdir = new File(args[x+1])
            x++
            break
        case '-o':
            outputdir=new File(args[x+1])
            x++
            break
        case '--copyoriginal':
            copyOriginal=true
            break
        case '--no-toc':
            doToc=false
            break
        case '--no-nav':
            doNav=false
            break
        case '--no-auto-clean':
            cleanUpAuto=false
            break
        case '--verbose':
            verbose=true
            break
        case '--separate-toc':
            tocAsIndex=false
            break
        case '-O':
            def arr=args[x+1].split('=',2)
            if(arr.length>1){
                options[arr[0]]=arr[1]
            }
            break
        default:
            pdocextra<<args[x]
    }
    x++
}
if(!docsdir){
    docsdir = new File(System.getProperty("user.dir"))
}
if(help){
    println "% Usage\n"
    println "    edam.groovy [-h/--help] [-d <basedir>] [-t <templatesdir>] [-o <outputdir>] [--no-toc] [--no-nav] [--verbose] [--clean] [--separate-toc]"
    println "    [-O option=value [ -O ... ] ]\n"
    println '''## Arguments

`-h/--help`
:   Show this help text.

`-d <basedir>`
:   The base directory containing the docs to convert. Defaults to the current directory.

`-t <templatesdir>`
:   The directory containing templates. Defaults to basedir/templates.

`-o <outputdir>`
:   The directory to write HTML files to. Defaults to the basedir.

`--no-toc`
:   Don't include the Table of Contents.

`--no-nav`
:   Don't include navigation links on each page.

`--verbose`
:   Be verbose about running pandoc

`--no-auto-clean`
:   Automatically clean up any generated files. Otherwise templates/toc.txt will be created.

`--separate-toc`
:   Put the Table of Contents on a separate page, instead of at the end of the index page.

`-O option=value`

:   Override an option value. Options are shown below.\n'''
    println "## Options\n"
    println "Options define the conventional defaults used for generating output.  You can override any value with `-O option=value` on the commandline.\n"
    options.keySet().each{
        println "${it}\n:    ${optionDescs[it]} Default: `${optionsDefaults[it]}`\n"
    }
    return 1
}
if(!tdir){
    tdir=new File(docsdir,'templates')   
}
if(!outputdir){
    outputdir=docsdir   
}
def toc= getToc(docsdir)
if(!toc){
    println "Empty toc"
    return 1
}
def templates=getTemplates(tdir)
if(verbose){
    println "running generateAll with options: ${options}"
}
generateAll(toc,templates,docsdir,outputdir)
tempFiles.each{f-> f.deleteOnExit()}
if(cleanUpAuto){
    autoFiles.each{f->f.deleteOnExit()}
}
