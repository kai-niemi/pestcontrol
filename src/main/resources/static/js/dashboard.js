const AppDashboard = function (settings) {
    this.settings = settings;
    this.init();
};

AppDashboard.prototype = {
    init: function () {
        var socket = new SockJS(this.settings.endpoints.socket),
                stompClient = Stomp.over(socket),
                _this = this;
        // stompClient.log = null;
        stompClient.log = (log) => {};
        stompClient.connect({}, function (frame) {
            stompClient.subscribe(_this.settings.topics.status, function (payload) {
                var _event = JSON.parse(payload.body);
                _this.handleStatusUpdate(_event);
            });

            stompClient.subscribe(_this.settings.topics.toast, function (payload) {
                var _event = JSON.parse(payload.body);
                _this.handleToastUpdate(_event);
            });

            stompClient.subscribe(_this.settings.topics.refresh, function (payload) {
                location.reload();
            });

            stompClient.subscribe(_this.settings.topics.update, function (payload) {
                $.getJSON("cluster/update", function(json) {});
            });
        });
    },

    getElement: function (id) {
        return $('#' + id);
    },

    handleStatusUpdate: function (node) {
        var _this = this;

        var divElt = _this.getElement("node-" +  node.nodeId);

        // Flash spinner for a few sec
        var _spinnerElt = divElt.find(".pc-spinner");
        _spinnerElt.attr('style','display: block');

        setTimeout(function () {
            _spinnerElt.attr('style','display: none');
        }, 2000);

        var _growElt = _spinnerElt.find(".spinner-grow");
        _growElt.removeClass( "text-success");
        _growElt.removeClass( "text-danger");
        _growElt.removeClass( "text-warning");

        // Update color styles
        divElt.removeClass( "alert-success");
        divElt.removeClass( "alert-danger");
        divElt.removeClass( "alert-warning");
        divElt.removeClass( "border-success");
        divElt.removeClass( "border-danger");
        divElt.removeClass( "border-warning");

        if (node.available === true) {
            divElt.addClass( "alert-success");
            divElt.addClass( "border-success");
            _growElt.addClass( "text-success");
        } else {
            divElt.addClass( "alert-warning");
            divElt.addClass( "border-warning");
            _growElt.addClass( "text-warning");
                // divElt.addClass( "alert-danger");
                // divElt.addClass( "border-danger");
                // _growElt.addClass( "text-danger");
        }

        // Update data elements
        divElt.find(".pc-last-active").text(node.lastActive);
    },

    handleToastUpdate: function (event) {
        var _this = this;

        const toastElt = _this.getElement('toast');

        toastElt.removeClass( "text-bg-danger");
        toastElt.removeClass( "text-bg-warning");
        toastElt.removeClass( "text-bg-success");

        if (event.messageType === 'error') {
            toastElt.addClass( "text-bg-danger");
        } else if (event.messageType === 'warning') {
            toastElt.addClass( "text-bg-warning");
        } else if (event.messageType === 'information') {
            toastElt.addClass( "text-bg-success");
        }

        const toastBody = toastElt.find(".toast-body");
        toastBody.text(event.message);

        const toastBootstrap = bootstrap.Toast.getOrCreateInstance(toastElt);
        toastBootstrap.show();
    }
};

document.addEventListener('DOMContentLoaded', function () {
    new AppDashboard({
        endpoints: {
            socket: '/pestcontrol-service',
        },

        topics: {
            status: '/topic/dashboard/status',
            toast: '/topic/dashboard/toast',
            refresh: '/topic/dashboard/refresh',
            update: '/topic/dashboard/update',
        },

        elements: {

        }
    });
});

