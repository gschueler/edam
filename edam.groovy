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
genTocOnly=false
cleanUpAuto=true
verbose=false
pagevars=[:]
optionsDefaults=[
        singleIndex:'true',
        chapterNumbers:'true',
        chapterTitle: 'Chapter',
        pageFileName:'${name}',
        cssFileName:'style.css',
        indexFileName:'(index|readme)',
        indexFileOutputName:'index',
        tocFileName:'toc',
        tocTitle:'Table of Contents',
        pandocCmd:"pandoc",
        tokenStart:'${',
        tokenEnd:'}',
        singleOutputFile:'false',
        pageLinkTitle: '${title}'
        ]
optionDescs=[
        singleIndex: 'true/false, if only a single markdown file, use it as the index HTML file.',
    chapterNumbers:'true/false, use chapter numbers in navigation.',
    chapterTitle:'Localized text to use for a chapter title.',
    pageFileName: 'Template for generated filename for each page, variables: `${index}`,`${title}`,`${name}`.',
    cssFileName:'File name of the css file to link in the HTML header.',
    indexFileName: 'File name base expected as index markdown file.',
    indexFileOutputName: 'File name base used as index HTML file.',
    tocFileName: 'File name expected/generated as table-of-contents file.',
    tocTitle: 'Localized text to use for table of contents title.',
    pandocCmd: 'Pandoc command to run.',
    tokenStart:'Variable expansion start token.',
    tokenEnd:'Variable expansion end token.',
    singleOutputFile:'true/false, combine all files into one output file.',
    pageLinkTitle: 'Template for title for links to the page, variables: `${index}`,`${title}`,`${name}`.',
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
    def str=title.trim()
    str= str.replaceAll(/['":\/<>,;\[\]\{\}\|\\!@#\$%\^&\*\(\)\+~`\?=]/,'')
    str= str.replaceAll(/[\s]/,'-')
    str = str.toLowerCase()
    str = str.replaceAll(/^[^a-z]+/,'')
    
    return str
}
def addTempFile(File file){
    file.deleteOnExit()
    file
}
def addAutoFile(File file){
    if(cleanUpAuto){
        file.deleteOnExit()
    }
    file
}
def writeTempFile(String content){
    def tfile = addTempFile(File.createTempFile("edam","temp"))
    tfile.text=content
    return tfile
}
def filenameToTitle(File file){
    file.name.replaceAll(/\.(txt|md)$/,'').replaceAll(/^(.)/,{a,m->m.toUpperCase()})
}
def pageLinkTitle(Map page){
    return replaceParams(options.pageLinkTitle,[chapterTitle:options.chapterTitle,title:page.title,index:page.index,name:page.file.name.replaceAll(/\.(md|txt)$/,'')])
}
def readIndexFile(File dir){
    def nameRegex="^"+options.indexFileName+'\\.(md|txt)$'
    def file=dir.listFiles().find{it.name=~nameRegex}
    def index=null
    def title=readTitle(file)
    if(file && !title){
        title = filenameToTitle(file)
    }
    if(title){
        title=replaceParams(title,pagevars,options.tokenStart,options.tokenEnd)
        index=[title:title,index:-1,outfile:"${options.indexFileOutputName}.html",file:file]
    }
    return index
}
def replaceParams(String templ,Map params,String tokenStart, String tokenEnd){
    def replaced=templ.replaceAll('('+java.util.regex.Pattern.quote(tokenStart)+'([a-zA-Z_0-9.-]+?)'+java.util.regex.Pattern.quote(tokenEnd)+')',{all,match1,match2->
            null!=params[match2]?params[match2]:match1
        })
    return replaced
}
def replaceParams(String templ,Map params){
    return replaceParams(templ,params,'${','}')
}
def expandFile(File file){
    pagevars?writeTempFile(replaceParams(file.text,pagevars,options.tokenStart,options.tokenEnd)):file
}
def readToc(File dir){
    def tocfile=new File(dir,"toc.conf")
    def toc=null
    if(tocfile.exists()){    
        toc=[]
        tocfile.eachLine { line ->
            def match=line=~/^(\d+):([^:]+):(.+)$/
            if(match.matches()){
                fdef=[index:match.group(1).toInteger(),file:new File(dir,match.group(2)),title:match.group(3).trim()]
                def identifier=titleToIdentifier(fdef.title)
                def params=[title:identifier,index:fdef.ndx,name:fdef.file.name.replaceAll(/\.(md|txt)$/,'')]
                def filestub=replaceParams(options.pageFileName,params)
                def teststub=filestub
                def x=1
                while(toc.find{it.outfile==teststub+".html"}){
                    x++
                    teststub=filestub+"-${x}"
                }
                fdef.outfile=teststub+".html"
                toc<<fdef
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
        addAutoFile tocfile
        //create toc.conf
        def ndx=1
        tocfile.withWriter { out ->
            mdfiles.each{file->
                def title=readTitle(file)
                if(!title){
                    title = filenameToTitle(file)
                }
                title=replaceParams(title,pagevars,options.tokenStart,options.tokenEnd)
                def outfile
                def identifier=titleToIdentifier(title)
                def params=[title:identifier,index:ndx,name:file.name.replaceAll(/\.(md|txt)$/,'')]
                def filestub=replaceParams(options.pageFileName,params)
                def teststub=filestub
                def x=1
                while(toc.find{it.outfile==teststub+".html"}){
                    x++
                    teststub=filestub+"-${x}"
                }
                outfile=teststub+".html"
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
        if(common.size()!=mdfiles.size() || common.size() !=readtoc.size()){
            println "Warning: some files changed compared to the TOC, please update toc.conf or remove it to regenerate: ${common}"
            return null
        }
        
        toc=readtoc
    }
    return toc
}

def createTocMdFile(File dir,toc,title,content=null){
    def tocout=new File(dir,"${options.tocFileName}.txt")
    if(!tocout.exists() && !genTocOnly){
        addAutoFile tocout
    }
    tocout.withWriter{writer->
        if(content || !genTocOnly){
            writer<< (content?:"% ${title}")
            writer<<"\n\n"
        }
        if(toc){
            if(content || genTocOnly){
                writer<<"## ${options.tocTitle}\n\n"
            }
            toc.each{titem->
                writer<<"${titem.index}. [${pageLinkTitle(titem)}](${titem.outfile})\n"
            }
        }
    }
}
defaultTemplates=[before:'''<div id="docbody">
''',after:'''</div>
''',html:'''<!DOCTYPE html>
<html>
<head>
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
</html>''',nav:'''<nav class="page $navclass$">
    <ul>
        <li class="current"><a href="$currentpagelink$">$currentpage$</a></li>
        $if(tocpage)$<li class="toc"><a href="$tocpagelink$">$tocpage$</a></li>$endif$
        $if(prevpage)$<li class="prev"><a href="$prevpagelink$">$prevpage$</a></li>$endif$
        $if(nextpage)$<li class="next"><a href="$nextpagelink$">$nextpage$</a></li>$endif$
    </ul>
</nav>''']

def getTemplates(File tdir){
    def templates=[:]
    defaultTemplates.keySet().each{k->
        templates[k]=new File(tdir,"${k}.template")
    }
    templates.each{k,v->
        if(!v.exists()){
            addAutoFile v
            if(!v.parentFile.exists()){
                addAutoFile v.parentFile
            }
            v.parentFile.mkdirs()
            v.withWriter{w->w.write(defaultTemplates[k])}
        }
    }
    return templates
}

def runPandoc(List longparams){
    def panargs = [options.pandocCmd]
    panargs.addAll pdocextra
    panargs.addAll longparams
    if(verbose){
        println "Run: ${panargs.join(' ')}"
    }
    return panargs.execute()
}
endif=java.util.regex.Pattern.quote('$endif$')
def replaceNavContent(Map navs,String navcontent){
    navs.keySet().each{k->
        navcontent=navcontent.replaceAll(/\$${k}\$/,{
            navs[k]
        })
    }
    //replace ifclauses
    def clauses=[(java.util.regex.Pattern.quote('$if(tocpage)$')):'tocpagelink',
                (java.util.regex.Pattern.quote('$if(nextpage)$')):'nextpagelink',
                (java.util.regex.Pattern.quote('$if(prevpage)$')):'prevpagelink']
    
    clauses.each{k,v->
        if(navs[v]){
            navcontent=navcontent.replaceAll(k+'(.*?)'+endif,'$1')
        }else{
            navcontent=navcontent.replaceAll(k+'(.*?)'+endif,'')
        }
    }
    navcontent
}

/**
 * Generate output files using pandoc. 
 * order of output:
 * -1: index (if it exists)
 * 0: toc (if doToc)
 * (-1 and 0 are merged if tocAsIndex)
 * 1: first page, ...
 */
def generateAll(toc,templates,File dir, File outdir){
    def ndxtoc=[]
    
    def index=readIndexFile(dir)
    if(index){
        ndxtoc<<index
    }
    if('true'==options.singleOutputFile){
        doToc=false
        doNav=false
    }
    
    def tocdoc
    if(doToc && toc.size()>1){
        tocdoc=[index:0,title:options.tocTitle,file:new File(dir,"${options.tocFileName}.txt"),outfile:'toc.html']
        ndxtoc<<tocdoc
    }
    
    if(tocAsIndex && tocdoc){
        tocdoc.outfile="${options.indexFileOutputName}.html"
        if(index){
            ndxtoc.remove(0)
            tocdoc.title=index.title
            tocdoc.content=index.file.text
        }
    }
    if(tocdoc){
        createTocMdFile(genTocOnly?outdir:dir,toc,tocdoc.title,tocdoc.content)
    }
    if(genTocOnly){
        return
    }
    
    def allpages=ndxtoc + toc
    
    if(1==allpages.size()){
        doNav=false
        if(options.singleIndex=='true'){
            allpages[0].outfile="${options.indexFileOutputName}.html"
        }
    }
    
    def navfileTop=addTempFile(new File(dir,'temp-nav-top.html'))
    def navfileBot=addTempFile(new File(dir,'temp-nav-bot.html'))
    
    if('true'==options.singleOutputFile){
        //list all markdown files into the multifiles of the first entry
        def multifiles=[]
        allpages.eachWithIndex{titem,x->
            if(x>0){
                multifiles<<expandFile(titem.file)
            }
        }
        
        allpages[0].multifiles=multifiles
        allpages[0].index=0
        if(options.singleIndex=='true'){
            allpages[0].outfile="${options.indexFileOutputName}.html"
        }
        allpages = [allpages[0]]
    }
    
    allpages.eachWithIndex{titem,x->
        def pargs=['-B',expandFile(templates.before)]
        if(doNav){
            def navcontent=templates.nav.text
            //set up nav links
            def navs=[currentpage:titem.index>0 && options.chapterNumbers=='true' ?"${options.chapterTitle} ${titem.index}: ${pageLinkTitle(titem)}":pageLinkTitle(titem),currentpagelink:titem.outfile]
            if(x<allpages.size()-1){
                //all but the last page
                navs.nextpage=allpages[x+1].index>0 && options.chapterNumbers=='true'?"${options.chapterTitle} ${allpages[x+1].index}: ${pageLinkTitle(allpages[x+1])}":pageLinkTitle(allpages[x+1])
                navs.nextpagelink=allpages[x+1].outfile
            }
            if(titem.index>1){
                //all content pages but the first
                navs.prevpage=allpages[x-1].index>0 && options.chapterNumbers=='true'?"${options.chapterTitle} ${allpages[x-1].index}: ${pageLinkTitle(allpages[x-1])}":pageLinkTitle(allpages[x-1])
                navs.prevpagelink=allpages[x-1].outfile
            }
            def toctitle=tocdoc?.title
            def toclink=tocdoc?.outfile
            if(tocdoc && titem.index!=tocdoc.index ){
                //all pages but the toc page
                navs.tocpage=tocdoc.title
                navs.tocpagelink=tocdoc.outfile
            }else if(!tocdoc && index && titem.index!=index.index){
                //all pages but the toc page
                navs.tocpage=index.title
                navs.tocpagelink=index.outfile
            }
            //write nav temp files
            navfileTop.withWriter{writer->
                def txt=replaceNavContent(navs + [navclass:'top'],navcontent)
                writer.write(pagevars?replaceParams(txt,pagevars):txt)
            }
            navfileBot.withWriter{writer->
                def txt=replaceNavContent(navs + [navclass:'bottom'],navcontent)
                writer.write(pagevars?replaceParams(txt,pagevars):txt)
            }
            pargs.addAll(['-B',navfileTop,'-A',navfileBot])
        }
        
        pargs.addAll(['-A',expandFile(templates.after)])
        def htemplate=expandFile(templates.html)
        pargs.addAll(['-o',new File(outdir,titem.outfile).absolutePath,'-s',"--css=${options.cssFileName}","--template=${htemplate.absolutePath}"])
        if(titem.index>0){
            pargs<<"--toc"
        }
        pargs.add(expandFile(titem.file))
        if(titem.multifiles){
            pargs.addAll titem.multifiles
        }
        def proc=runPandoc(pargs)
        proc.consumeProcessOutput(System.out,System.err)
        def result=proc.waitFor()
        if(0!=result){
            println "Error running pandoc, result: ${result}"
            return
        }
        
    }
}
def docsdir
def tdir
def outputdir
def x=0
def xtraargs = false
while(x<args.length){
    if(!xtraargs){
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
            case '--gen-toc-only':
                genTocOnly=true
                break
            case '--variables':
                Properties p = new Properties()
                p.load(new File(args[x+1]).newReader())
                pagevars.putAll(p)
            case '-V':
                def arr=args[x+1].split('=',2)
                if(arr.length>1){
                    pagevars[arr[0]]=arr[1]
                }
                x++
                break
            case '-O':
                def arr=args[x+1].split('=',2)
                if(arr.length>1){
                    options[arr[0]]=arr[1]
                }
                x++
                break
            case '-x':
                xtraargs=true
        }
    }else{
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
    println "    [-O option=value [ -O ... ] ] [-V var=value ...] [ --variables <propertiesfile> ] [-x [ extra pandoc args .. ] ]\n"
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

`-V var=value`
:   Define a variable to expand within templates and markdown content.

`--variables <propertiesfile>`
:   Load variables from a properties file.

`-O option=value`

:   Override an option value. Options are shown below.\n'''
    println "## Options\n"
    println "Options define the conventional defaults used for generating output.  You can override any value with `-O option=value` on the commandline.\n"
    optionDescs.keySet().each{
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
def templates=getTemplates(tdir)
if(verbose){
    println "running generateAll with options: ${options}"
}
generateAll(toc,templates,docsdir,outputdir)
