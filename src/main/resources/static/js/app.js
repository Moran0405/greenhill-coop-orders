/* Member ordering page logic */
(function () {
    "use strict";

    var state = {
        role: null,
        memberId: null,
        memberName: null,
        currentRound: null,
        products: [],
        basket: {}          // productId -> quantity (string)
    };

    function $(id) { return document.getElementById(id); }

    function show(el) { el.classList.remove("hidden"); }
    function hide(el) { el.classList.add("hidden"); }

    function api(method, url, body) {
        return fetch(url, {
            method: method,
            headers: { "Content-Type": "application/json" },
            credentials: "same-origin",
            body: body ? JSON.stringify(body) : undefined
        }).then(function (res) {
            return res.json().catch(function () { return {}; }).then(function (data) {
                if (!res.ok) {
                    throw new Error(data.error || ("Request failed (" + res.status + ")"));
                }
                return data;
            });
        });
    }

    function money(value) {
        return "$" + Number(value).toFixed(2);
    }

    function fmtDate(iso) {
        if (!iso) return "";
        var d = new Date(iso);
        return d.toLocaleDateString("en-AU", { weekday: "short", day: "numeric", month: "short" }) +
            " " + d.toLocaleTimeString("en-AU", { hour: "2-digit", minute: "2-digit" });
    }

    /* ---------- rendering ---------- */

    function renderSession() {
        if (state.role === "member") {
            $("whoami").textContent = "Signed in as " + state.memberName;
            hide($("signin-panel"));
            show($("member-panel"));
            loadRound();
        } else {
            $("whoami").textContent = "";
            show($("signin-panel"));
            hide($("member-panel"));
        }
    }

    function renderRound() {
        var banner = $("round-banner");
        if (!state.currentRound) {
            banner.innerHTML = "<strong>No round is open at the moment.</strong> Please check back on Monday.";
            banner.className = "round-banner closed";
            show(banner);
            return;
        }
        var r = state.currentRound;
        banner.className = "round-banner";
        banner.innerHTML =
            "<div><span class='badge'>Round open</span> <strong>" + r.name + "</strong></div>" +
            "<div>Orders close: " + fmtDate(r.closesAt) + "</div>";
        show(banner);
    }

    function renderProducts() {
        var grid = $("product-grid");
        grid.innerHTML = "";
        if (!state.products.length) {
            grid.innerHTML = "<p class='empty-note'>No products in the catalogue yet.</p>";
            return;
        }
        state.products.forEach(function (p) {
            var card = document.createElement("div");
            card.className = "product-card";
            var unitTag = p.pricingType === "WEIGHT" ? "per kg" : "each";
            var step = p.pricingType === "WEIGHT" ? "0.1" : "1";
            var placeholder = p.pricingType === "WEIGHT" ? "e.g. 1.5" : "e.g. 2";
            card.innerHTML =
                "<div class='name'>" + p.name + "</div>" +
                "<div class='desc'>" + (p.description || "") + "</div>" +
                "<div class='price'>" + money(p.price) +
                " <span class='unit-tag'>" + unitTag + "</span></div>" +
                "<input type='number' min='0' step='" + step + "' placeholder='" + placeholder +
                "' data-product='" + p.id + "'>";
            grid.appendChild(card);
        });
        // restore existing basket values
        Object.keys(state.basket).forEach(function (pid) {
            var input = grid.querySelector("input[data-product='" + pid + "']");
            if (input) input.value = state.basket[pid];
        });
        grid.addEventListener("input", readBasket);
    }

    function readBasket() {
        state.basket = {};
        $("product-grid").querySelectorAll("input[data-product]").forEach(function (input) {
            var v = input.value.trim();
            if (v !== "" && Number(v) > 0) {
                state.basket[input.getAttribute("data-product")] = v;
            }
        });
        renderBasket();
    }

    function renderBasket() {
        var basket = $("basket");
        var totalBox = $("basket-total");
        basket.innerHTML = "";
        var keys = Object.keys(state.basket);
        if (!keys.length) {
            basket.innerHTML = "<p class='empty-note'>Nothing in your order yet - add items from the catalogue.</p>";
            totalBox.textContent = "";
            return;
        }
        var total = 0;
        keys.forEach(function (pid) {
            var p = state.products.find(function (x) { return String(x.id) === pid; });
            if (!p) return;
            var qty = Number(state.basket[pid]);
            var lineTotal = qty * p.price;
            total += lineTotal;
            var row = document.createElement("div");
            row.className = "basket-line";
            var unitLabel = p.pricingType === "WEIGHT" ? "kg" : "units";
            row.innerHTML =
                "<span class='line-name'>" + p.name + " <em>(" + qty + " " + unitLabel + ")</em></span>" +
                "<span class='line-amount'>" + money(lineTotal) + "</span>";
            basket.appendChild(row);
        });
        totalBox.textContent = "Estimated total: " + money(total);
    }

    function renderMyOrder(order) {
        var statusBox = $("order-status");
        if (!order) {
            statusBox.innerHTML = "<span class='empty-note'>You have not ordered this round yet.</span>";
            hide($("cancel-order"));
            return;
        }
        statusBox.innerHTML = "Saved order <strong>#" + order.id + "</strong>, last updated " +
            fmtDate(order.updatedAt) +
            (order.editable ? " &mdash; <span style='color:var(--green)'>still editable</span>"
                : " &mdash; <strong>round closed, read only</strong>");
        if (order.editable) {
            show($("cancel-order"));
        } else {
            hide($("cancel-order"));
        }
        // reflect the saved order into the basket
        state.basket = {};
        order.items.forEach(function (item) {
            state.basket[item.productId] = item.quantity;
        });
        renderProducts();
        renderBasket();
    }

    /* ---------- data ---------- */

    function loadRound() {
        api("GET", "/api/rounds/current").then(function (round) {
            state.currentRound = round;
            renderRound();
            return api("GET", "/api/products?activeOnly=true");
        }).then(function (products) {
            state.products = products || [];
            renderProducts();
            renderBasket();
            if (state.currentRound) {
                fetchMyOrder();
            }
        }).catch(function (err) { alert(err.message); });
    }

    function fetchMyOrder() {
        api("GET", "/api/rounds/" + state.currentRound.id + "/has-order")
            .then(function (r) {
                if (!r.hasOrder) { renderMyOrder(null); return null; }
                return api("GET", "/api/rounds/" + state.currentRound.id + "/my-order");
            })
            .then(function (order) { if (order) renderMyOrder(order); })
            .catch(function (err) {
                if (String(err.message).indexOf("No order") === -1) showError(err.message);
            });
    }

    function showError(msg) {
        var box = $("order-error");
        box.textContent = msg;
        show(box);
    }

    function clearError() { hide($("order-error")); }

    /* ---------- events ---------- */

    $("signin-form").addEventListener("submit", function (e) {
        e.preventDefault();
        clearError();
        api("POST", "/api/auth/login", { email: $("email").value })
            .then(function (data) {
                state.role = "member";
                state.memberId = data.memberId;
                state.memberName = data.memberName;
                renderSession();
            })
            .catch(function (err) {
                $("signin-error").textContent = err.message;
                show($("signin-error"));
            });
    });

    $("signout-btn").addEventListener("click", function () {
        api("POST", "/api/auth/logout", {}).then(function () {
            state = { role: null, memberId: null, memberName: null, currentRound: null, products: [], basket: {} };
            renderMyOrder(null);
            renderSession();
        });
    });

    $("save-order").addEventListener("click", function () {
        readBasket();
        clearError();
        if (!state.currentRound) { showError("No round is open for ordering."); return; }
        var items = Object.keys(state.basket).map(function (pid) {
            return { productId: Number(pid), quantity: state.basket[pid] };
        });
        if (!items.length) { showError("Add at least one item before saving."); return; }
        api("PUT", "/api/rounds/" + state.currentRound.id + "/my-order", { items: items })
            .then(function (order) {
                renderMyOrder(order);
            })
            .catch(function (err) { showError(err.message); });
    });

    $("cancel-order").addEventListener("click", function () {
        clearError();
        if (!state.currentRound) return;
        if (!confirm("Cancel your whole order for this round?")) return;
        api("DELETE", "/api/rounds/" + state.currentRound.id + "/my-order", {})
            .then(function () {
                state.basket = {};
                renderProducts();
                renderBasket();
                renderMyOrder(null);
            })
            .catch(function (err) { showError(err.message); });
    });

    /* ---------- boot ---------- */
    api("GET", "/api/auth/me").then(function (me) {
        if (me.role === "member") {
            state.role = "member";
            state.memberId = me.memberId;
            state.memberName = me.memberName;
        }
        renderSession();
    });
})();
