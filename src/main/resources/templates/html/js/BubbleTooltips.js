/*javascript for Bubble Tooltips by Alessandro Fulciniti
- http://pro.html.it - http://web-graphics.com */
//Modified by Brenton Fletcher

function enableTooltips(id){
var h;
if(!document.getElementById || !document.getElementsByTagName) return;
AddCss();
h=document.createElement("span");
h.id="btc";
h.style.position="absolute";
document.getElementsByTagName("body")[0].appendChild(h);
doTooltips(id);
}

function doTooltips(id)
{
   var links;
   if(id==null) links=document.getElementsByTagName("span");
   else links=document.getElementById(id).getElementsByTagName("span");
   for(var i=0;i<links.length;i++) Prepare(links[i]);
}

function Prepare(el){
var tooltip,t,s;
t=el.getAttribute("title");
if(t==null || t.length==0) return;
el.removeAttribute("title");
tooltip=CreateEl("span","tooltip");
s=CreateEl("span","top");
s.appendChild(document.createTextNode(t));
tooltip.appendChild(s);
setOpacity(tooltip);
el.tooltip=tooltip;
el.onmouseover=showTooltip;
el.onmouseout=hideTooltip;
el.onmousemove=Locate;
}

function showTooltip(e){
document.getElementById("btc").appendChild(this.tooltip);
Locate(e);
}

function hideTooltip(e){
var d=document.getElementById("btc");
if(d.childNodes.length>0) d.removeChild(d.firstChild);
}

function setOpacity(el){
el.style.opacity="0.95";
}

function CreateEl(t,c){
var x=document.createElement(t);
x.className=c;
x.style.display="block";
return(x);
}

function AddCss(){
var l=CreateEl("link");
l.setAttribute("type","text/css");
l.setAttribute("rel","stylesheet");
l.setAttribute("href","bt.css");
l.setAttribute("media","screen");
document.getElementsByTagName("head")[0].appendChild(l);
}

function Locate(e){
var btc=document.getElementById("btc");
var posx=0,posy=0;
if(e==null) e=window.event;
if(e.pageX || e.pageY){
    posx=e.pageX; posy=e.pageY;
    }
else if(e.clientX || e.clientY){
    posx=e.clientX+(document.documentElement.scrollLeft||document.body.scrollLeft);
    posy=e.clientY+(document.documentElement.scrollTop||document.body.scrollTop);
    }
btc.style.top=(posy+10)+"px";
btc.style.left=(posx-20)+"px";
}
