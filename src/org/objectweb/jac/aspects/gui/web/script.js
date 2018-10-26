// <!-- Begin
/**
 * Commits the form the element is in
 *
 * @param element HTML element the key was pressed in
 * @param value eventAndAction value
 */
function commitForm(element,value) {
    while(element.tagName.toLowerCase()!="form" && element.tagName.toLowerCase()!="body") {
        element = element.parentNode;
    }
    if (element.tagName.toLowerCase()=="form") {
        //alert("submitting form with eventAndAction="+value);
        hidden = document.createElement("input");
        hidden.setAttribute("type","hidden");
        hidden.setAttribute("name","eventAndAction");
        hidden.setAttribute("value",value);
        element.appendChild(hidden);
        element.submit();
    }
    return false;
}

/**
 * Commits the form if key==ENTER
 * @param key pressed key
 * @param element HTML element the key was pressed in
 * @param value eventAndAction value
 */
function commitFormOnEnter(event,element,value) {
    if (window.event) {
        key = window.event.keyCode;
    } else {
        key = event.keyCode;
    }
    if (key==13) {
        commitForm(element,value);
        return false;
    } else {
        return true;
    }
}

/**
 * Focus an HTML element with a given id
 *
 * @param id of HTML to focus
 */
function focus(id) {
    getElementById(id).focus();
}

/**
 * Browser version snooper; determines your browser
 * (Navigator 4, Navigator 6, or Internet Explorer 4/5)
 */
var isNav4, isNav6, isIE4;
function setBrowser()
{
    if (navigator.appVersion.charAt(0) == "4") {
        if (navigator.appName.indexOf("Explorer") >= 0) {
            isIE4 = true;
        } else {
            isNav4 = true;
        }
    } else if (navigator.appVersion.charAt(0) > "4") {
        isNav6 = true;
    }
}
setBrowser();

/**
 * Gets an HTML element with the given Id
 * @param id the id the look for
 */
function getElementById(id) {
    if (isNav6) {
        return document.getElementById(id);
    } else if (isNav4) {
        return document[id];
    } else {
        return document.all[id];
    }
}


/*
 *
 * Given an id and a property (as strings), return
 * the given property of that id.  Navigator 6 will
 * first look for the property in a tag; if not found,
 * it will look through the stylesheet.
 *
 * Note: do not precede the id with a # -- it will be
 * appended when searching the stylesheets
 *
 */
function getIdProperty(id, property)
{
    if (isNav6) {
        var styleObject = document.getElementById(id);
        if (styleObject != null) {
            styleObject = styleObject.style;
            if (styleObject[property]) {
                return styleObject[property];
            }
        }
        styleObject = getStyleBySelector( "#" + id );
        return (styleObject != null) ?
            styleObject[property] :
            null;
    } else if (isNav4) {
        return document[id][property];
    } else {
        return document.all[id].style[property];
    }
}


/*
 *
 * Given a selector string, return a style object
 * by searching through stylesheets. Return null if
 * none found
 *
 */
function getStyleBySelector(selector)
{
    if (!isNav6) {
        return null;
    }
    var sheetList = document.styleSheets;
    var ruleList;
    var i, j;

    /* look through stylesheets in reverse order that
       they appear in the document */
    for (i=sheetList.length-1; i >= 0; i--)
    {
        ruleList = sheetList[i].cssRules;
        for (j=0; j<ruleList.length; j++)
        {
            if (ruleList[j].type == CSSRule.STYLE_RULE &&
                ruleList[j].selectorText == selector)
            {
                return ruleList[j].style;
            }   
        }
    }
    return null;
}

/**
 * Gets the property of an element
 */
function getProperty(element,property) {
    if (isNav6) {
        var styleObject;
        if (element != null) {
            styleObject = element.style;
            if (styleObject[property]) {
                return styleObject[property];
            }
        }
        /*alert('No style property '+property);*/
        styleObject = getStyleBySelector( "#" + element.id );
        return (styleObject != null) ?
            styleObject[property] :
            null;
    } else if (isNav4) {
        return element[property];
    } else {
        return element.style[property];
    }
}

/*
 *
 * Given an id and a property (as strings), set
 * the given property of that id to the value provided.
 *
 * The property is set directly on the tag, not in the
 * stylesheet.
 *
 */
function setIdProperty(id, property, value)
{
    if (isNav6) {
        var styleObject = document.getElementById(id);
        if (styleObject != null) {
            styleObject = styleObject.style;
            styleObject[ property ] = value;
        }
        
        /*
          styleObject = getStyleBySelector( "#" + id );
          if (styleObject != null)
          {
          styleObject[property] = value;
          }
		*/
    } else if (isNav4) {
        document[id][property] = value;
    } else if (isIE4) {
        document.all[id].style[property] = value;
    }
}

/**
 * Scroll down to bottom of page
 */
function scrollToBottom() {
    window.scroll(0,10000);
}

function openURL(url) {
    window.location.href = url;
}

//  End -->

