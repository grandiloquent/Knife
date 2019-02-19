window['dom'] = {
    append: function (target, element) {
        if (typeof element == 'undefined') {
            element = target;
            target = document.body;
        }
        target.appendChild(element);
    },
    prepend: function (target, element) {
        if (typeof element == 'undefined') {
            element = target;
            target = document.body;
        }
        target.insertBefore(element, target.firstChild);
    },
    before: function (target, element) {
        
        target.parentNode.insertBefore(element, target);
    },
    after: function (target, element) {
        
        target.parentNode.insertBefore(element, target.nextSibling);
    },
};