var app = function () {

    // Model ------------------------------------------------------------------

    var socket;
    var pitCount = 6;
    var initPitVal = 6;
    var initStoreVal = 0;

    var Side = _.keyBy([ 'NORTH', 'SOUTH' ], _.identity);

    var initPits = function () {
        return _.map(_.range(pitCount), function () {
            return m.prop(initPitVal);
        })
    };

    var initStore = function () {
        return m.prop(initStoreVal);
    };

    var state = {
        side:      m.prop(),
        nextTurn:  m.prop(Side.NORTH),
        started:   m.prop(),
        abandoned: m.prop(),
        winner:    m.prop(),
        stores:    {},
        pits:      {}
    };

    _.forEach(Side, function (side) { state.stores[side] = initStore(); });
    _.forEach(Side, function (side) { state.pits[side] = initPits(); });


    // View -------------------------------------------------------------------

    var toCssClass = _.toLower;

    var Pit = {
        controller: function (args) {
            var idx = args.idx;
            this.increment = function () {
                socket.send("MOVE " + idx);
            };
            this.actionable = function () {
                return state.side() === args.side
                    && state.nextTurn() === args.side
                    && state.started()
                    && !state.abandoned();
            }
        },
        view: function (ctrl, args) {
            var isActionable = ctrl.actionable();
            return m('div.pit', {
                onclick: isActionable ? ctrl.increment : _.noop,
                class: isActionable ? 'actionable' : ''
            }, [
                m('div.stones', [
                    m('span', args.model())
                ])
            ]);
        }
    };

    var Store = {
        view: function (ctrl, side) {
            return m('div.store', {
                class: toCssClass(side)
            }, [
                m.component(Pit, {
                    side: side,
                    model: state.stores[side]
                })
            ]);
        }
    };

    var Row = {
        view: function (ctrl, side) {
            return m('div.row', {
                class: toCssClass(side)
            }, [
                _.map(_.range(pitCount), function (idx) {
                    return m.component(Pit, {
                        idx: idx,
                        side: side,
                        model: state.pits[side][idx]
                    });
                })
            ]);
        }
    };

    var Board = {
        view: function () {
            return m('div.board', [
                m.component(Store, Side.NORTH),
                m('div.pits', [
                    m.component(Row, Side.NORTH),
                    m.component(Row, Side.SOUTH)
                ]),
                m.component(Store, Side.SOUTH)
            ]);
        }
    };

    var Player = {
        controller: function (side) {
            return {
                name: function () {
                    if (side === state.side()) {
                        return 'you';
                    }

                    if (!state.started()) {
                        return 'waiting for opponent';
                    }

                    if (state.abandoned()) {
                        return 'opponent disconnected';
                    }

                    return 'opponent';
                },
                isActive: function () {
                    return side === state.nextTurn();
                },
                isWinner: function () {
                    return side === state.winner();
                }
            }
        },
        view: function (ctrl) {
            var klass = '';
            if (ctrl.isWinner()) {
                klass = 'winner';
            } else if (ctrl.isActive()) {
                klass = 'active';
            }
            return m('p.player', { class: klass }, ctrl.name());
        }
    };

    var Error = {
        view: function (ctrl, message) {
            return m('div.error', message);
        }
    };

    var Page = {
        view: function () {
            return m('div.page', [
                m.component(Player, Side.NORTH),
                m.component(Board),
                m.component(Player, Side.SOUTH)
            ]);
        }
    };

    if (!window.WebSocket) {
        m.mount(document.body, m.component(Error, "Web socket support is required."));
        return;
    }

    m.mount(document.body, m.component(Page));


    // Event handlers ---------------------------------------------------------

    socket = new WebSocket("ws://" + location.host + "/game");

    var handlers = {
        "JOINED": function (parts) {
            var side = parts.shift();
            state.side(side);
        },
        "STARTED": function () {
            state.started(true);
        },
        "UPDATED": function (parts) {
            var payload = parts.shift();
            var nextTurn = parts.shift();
            var pits = JSON.parse(payload);

            var i = 0;
            _.forEach([Side.NORTH, Side.SOUTH], function (side) {
                _.forEach(_.range(pitCount), function (idx) {
                    state.pits[side][idx](pits[i++]);
                });
                state.stores[side](pits[i++]);
            });

            state.nextTurn(nextTurn ? nextTurn : null);
        },
        "FINISHED": function (parts) {
            state.winner(parts.shift());
        },
        "ABANDONED": function () {
            state.abandoned(true);
        }
    };

    var handle = function (payload) {
        console.log(payload);
        var parts = payload.split(' ');
        var type = parts.shift();
        handlers[type](parts);
    };

    socket.onmessage = function (event) {
        try {
            m.startComputation();
            handle(event.data);
        } finally {
            m.endComputation();
        }
    };

    socket.onopen = function () {
        if (socket.readyState == WebSocket.OPEN) {
            socket.send("JOIN");
        }
    };

    socket.onerror = function (event) {
        console.log(event);
    };

    socket.onclose = function () {
        console.log("Server disconnected.");
    };

};

document.addEventListener('DOMContentLoaded', app);
