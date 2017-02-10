window.ReactDOM = require('react-dom');
window.React    = require('react');
window.CSSTransitionGroup = require('react-addons-css-transition-group');

console.log("css stuff");
console.log(window.CSSTransitionGroup);

var injectTapEventPlugin = require('react-tap-event-plugin');
console.log("Injecting tapEventPlugin...");
injectTapEventPlugin();

window.hljs = require("highlight.js");
require("highlight.js/styles/github.css");

// //images
// window.googleMapImage      = require("../images/googleMap.png");
// window.reactListViewImage  = require("../images/reactListView.png");
// window.reactTreeViewImage  = require("../images/reactTreeView.png");
// window.elementaluiImage    = require("../images/elementalui.png");
// window.materialuiImage     = require("../images/mui.png");
// window.semanticuiImage     = require("../images/semanticui.png");
// window.reactTableImage     = require("../images/reactTable.png");
// window.bottomTearImage     = require("../images/bottom-tear.svg");
// window.reactTagsInputImage = require("../images/reactTagsInput.png");
// window.reactSelectImage    = require("../images/reactSelect.png");
// window.reactInfiniteImage  = require("../images/reactInfinite.png");
// window.reactGeomIconImage  = require("../images/reactGeomIcon.png");
// window.spinnerImage        = require("../images/spinner.png");
// window.reactPopoverImage   = require("../images/reactPopover.png");
// window.reactDraggableImage = require("../images/reactDraggable.png");
