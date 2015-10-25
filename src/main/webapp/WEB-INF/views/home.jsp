<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<style type="text/css">
.treetable {
	border-collapse:separate;
}
.treetable th {
	background:#4682B4;
	color:#FFFFFF;
}
.treetable td, th {
	padding:3px;
	border:1px solid #D3D3D3;
	
}
.treetable label a{
	cursor:pointer;
	color:#000000;
	font-weight:bold;
	padding-left:16px; 
}
.treetable tr {
	display:none;
}
.treetable thead tr {
   background:#EFEFEA;
	display:table-row;
}
.treetable .lev1 {
	background:#FFFFFA;	
	display:table-row;
}
.lev2 {
	background:#EFEFEA;
	
}
.lev3 {
	background:#FFFFF5;
}
input[type="checkbox"]{
	display:none;
}
input + a {
	background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAsAAAALCAIAAAAmzuBxAAAACXBIWXMAAAsSAAALEgHS3X78AAAAkElEQVQYlXWOvRWDQAyDv/DYK2wQSro8OkpGuRFcUjJCRmEE0TldCpsjPy9qzj7Jki62Pgh4vnqbbbEWuN+use/PlArwHccWGg780psENGFY6W4YgxZIAM339WmT3m397YYxxn6aASslFfVotYLTT3NwcuTKlFpNR2sdEak4acdKeafPlE2SZ7sw/1BEtX94AXYTVmyR94mPAAAAAElFTkSuQmCC)
	no-repeat 0px 5px;
}
input:checked + a{
	background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAsAAAALCAIAAAAmzuBxAAAACXBIWXMAAAsSAAALEgHS3X78AAAAeklEQVQYlX2PsRGDMAxFX3zeK9mAlHRcupSM4hFUUjJCRpI70VHIJr7D8BtJ977+SQ9Zf7isVG16WSQC0/D0OW/FqoBlDFkIVJ2xAhA8sI/NHbcYiFrPfI0fGklKagDx2F4ltdtaM0J9L3dxcVxi+zv62E+MwPs7c60dClRP6iug7wUAAAAASUVORK5CYII=)
	no-repeat 0px 5px;
}


</style>

<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello world!  
</h1>

<P>  The time on the server is ${serverTime}. </P>
<br><br>

<script type="text/javascript">        
/*<!--$(document).ready(-->
(function ($) {
    AJS.toInit(*/
function ResetAll() {
	var cs = document.getElementsByTagName('input');
	for (i=0; i < cs.length; i++) {
		if (cs[i].type == 'checkbox') {
			cs[i].checked = false;
		}
	}
}

function ShowLevel(row,lv) {
	var tBody = row.parentNode;
	var i = row.rowIndex;
	row = tBody.rows[i]; // Попытка перейти к следующей строке
	while (row && row.className.substring(3)*1 > lv) {
		if (row.className.substring(3)*1 == lv+1) {
			row.style.display = 'table-row';
			if ((row.querySelector('td input')) && row.querySelector('td input').checked) {
				ShowLevel(row,lv+1);
			}
		}
		i+=1;
		row = tBody.rows[i];
	}
}

function HideLevel(row,lv) {
	var i = row.rowIndex;
	var tBody = row.parentNode;
	row = tBody.rows[i]; // Попытка перейти к следующей строке
	while (row && row.className.substring(3)*1 > lv) {
		row.style.display = 'none';
		i+=1;
		row = tBody.rows[i];
	}
}

function sh(el) {

	var row = el.parentNode.parentNode.parentNode;
	var lv = row.className.substring(3)*1; // Уровень строки, циферка после 'lev'
	var newch = row.querySelector('td input').checked;
	var ua = window.navigator.userAgent;
	
	if(((ua.indexOf('MSIE') != -1)||(ua.indexOf('Firefox') != -1)||(ua.indexOf('Mozilla') != -1))&&(ua.indexOf('Chrome') == -1)) {
    	row.querySelector('td input').checked = !row.querySelector('td input').checked;
    	newch = !row.querySelector('td input').checked;    	
    }
	if (newch) {
		HideLevel(row,lv);
	} else {
		ShowLevel(row,lv);
	}
	/*var row = el.parentNode.parentNode.parentNode;
	var nextRow = row.parentNode.rows[row.rowIndex + 1];
	var chk = nextRow.querySelector('td > input');
	chk.checked = !chk.checked;*/
	
}

function SwapAll(b) {
	var tbl = document.getElementsByClassName('treetable')[0];
	for (i=1; i < tbl.rows.length; i++) {
		if (tbl.rows[i].className != 'lev1') {
			if (b) {tbl.rows[i].style.display = 'table-row';}
			else {tbl.rows[i].style.display = 'none';}
		}

		if (tbl.rows[i].querySelector('td input')) {tbl.rows[i].querySelector('td input').checked = b;}
	}
}
/*);
})(AJS.$);*/
        
</script> 


${reportHtml}
</body>
</html>
