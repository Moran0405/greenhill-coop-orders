/* Coordinator desk page logic */
(function () {
    "use strict";

    var state = { signedIn: false, rounds: [], selectedRoundId: null };

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
                if (!res.ok) throw new Error(data.error || ("Request failed (" + res.status + ")"));
                return data;
            });
        });
    }

    function money(v) { return "$" + Number(v).toFixed(2); }

    function fmt(iso) {
        if (!iso) return "";
        return new Date(iso).toLocaleString("en-AU", {
            day: "numeric", month: "short", hour: "2-digit", minute: "2-digit"
        });
    }

    function flash(id, msg, ok) {
        var box = $(id);
        box.textContent = msg;
        box.className = ok ? "ok-msg" : "error-msg";
        show(box);
        setTimeout(function () { hide(box); }, 5000);
    }

    /* ---------- rounds ---------- */

    function loadRounds() {
        return api("GET", "/api/rounds").then(function (rounds) {
            state.rounds = rounds || [];
            var select = $("round-select");
            select.innerHTML = "";
            state.rounds.forEach(function (r) {
                var opt = document.createElement("option");
                opt.value = r.id;
                opt.textContent = r.name + " (" + r.status + ")";
                select.appendChild(opt);
            });
            // Prefer the currently open round
            var open = state.rounds.find(function (r) { return r.status === "OPEN"; });
            select.value = open ? open.id : (state.rounds[0] ? state.rounds[0].id : "");
            state.selectedRoundId = select.value ? Number(select.value) : null;
            renderRoundList();
        });
    }

    function renderRoundList() {
        var wrap = $("round-list");
        wrap.innerHTML = "";
        if (!state.rounds.length) {
            wrap.innerHTML = "<p class='empty-note'>No rounds created yet.</p>";
            return;
        }
        var table = document.createElement("table");
        table.className = "coop";
        table.innerHTML = "<thead><tr><th>Round</th><th>Status</th><th>Opens</th><th>Closes</th></tr></thead><tbody></tbody>";
        var tbody = table.querySelector("tbody");
        state.rounds.forEach(function (r) {
            var tr = document.createElement("tr");
            tr.innerHTML = "<td>" + r.name + "</td>" +
                "<td><span class='status-chip status-" + r.status + "'>" + r.status + "</span></td>" +
                "<td>" + fmt(r.opensAt) + "</td><td>" + fmt(r.closesAt) + "</td>";
            tbody.appendChild(tr);
        });
        wrap.appendChild(table);
    }

    function loadRoundData() {
        if (!state.selectedRoundId) {
            $("summary-table").querySelector("tbody").innerHTML = "";
            $("orders-table").querySelector("tbody").innerHTML = "";
            $("summary-round-name").textContent = "-";
            return;
        }
        var round = state.rounds.find(function (r) { return r.id === state.selectedRoundId; });
        $("summary-round-name").textContent = round ? round.name : "";

        api("GET", "/api/rounds/" + state.selectedRoundId + "/summary").then(function (lines) {
            var tbody = $("summary-table").querySelector("tbody");
            tbody.innerHTML = "";
            (lines || []).forEach(function (l) {
                var tr = document.createElement("tr");
                tr.innerHTML = "<td>" + l.productName + "</td>" +
                    "<td>" + (l.pricingType === "WEIGHT" ? "per kg" : "per unit") + "</td>" +
                    "<td class='sum-qty'>" + Number(l.totalQuantity) + "</td>" +
                    "<td>" + money(l.estimatedCost) + "</td>";
                tbody.appendChild(tr);
            });
            if (!lines || !lines.length) {
                tbody.innerHTML = "<tr><td colspan='4' class='empty-note'>No orders in this round yet.</td></tr>";
            }
        });

        api("GET", "/api/rounds/" + state.selectedRoundId + "/orders").then(function (orders) {
            var tbody = $("orders-table").querySelector("tbody");
            tbody.innerHTML = "";
            (orders || []).forEach(function (o) {
                var tr = document.createElement("tr");
                var items = o.items.map(function (i) {
                    return i.productName + " x " + i.quantity;
                }).join(", ");
                tr.innerHTML = "<td>" + o.memberName + "</td>" +
                    "<td>" + items + "</td>" +
                    "<td>" + money(o.total) + "</td>" +
                    "<td>" + fmt(o.updatedAt) + "</td>";
                tbody.appendChild(tr);
            });
            if (!orders || !orders.length) {
                tbody.innerHTML = "<tr><td colspan='4' class='empty-note'>No orders in this round yet.</td></tr>";
            }
        });
    }

    /* ---------- products ---------- */

    function loadProducts() {
        var activeOnly = !$("show-inactive").checked;
        api("GET", "/api/products?activeOnly=" + activeOnly).then(function (products) {
            var tbody = $("products-table").querySelector("tbody");
            tbody.innerHTML = "";
            (products || []).forEach(function (p) {
                var tr = document.createElement("tr");
                tr.innerHTML = "<td>" + p.name + (p.description ? "<br><small>" + p.description + "</small>" : "") + "</td>" +
                    "<td>" + (p.pricingType === "WEIGHT" ? "by weight" : "whole unit") + "</td>" +
                    "<td>" + money(p.price) + "</td>" +
                    "<td>" + (p.active ? "<span class='status-chip status-OPEN'>Active</span>"
                        : "<span class='status-chip status-PACKED'>Deactivated</span>") + "</td>" +
                    "<td>" + (p.active ? "<button class='btn btn-danger btn-sm' data-deactivate='" + p.id + "'>Deactivate</button>" : "") + "</td>";
                tbody.appendChild(tr);
            });
            tbody.querySelectorAll("[data-deactivate]").forEach(function (btn) {
                btn.addEventListener("click", function () {
                    api("DELETE", "/api/products/" + btn.getAttribute("data-deactivate"), {})
                        .then(loadProducts)
                        .catch(function (err) { flash("product-msg", err.message, false); });
                });
            });
        });
    }

    /* ---------- members ---------- */

    function loadMembers() {
        api("GET", "/api/members").then(function (members) {
            var tbody = $("members-table").querySelector("tbody");
            tbody.innerHTML = "";
            (members || []).forEach(function (m) {
                var tr = document.createElement("tr");
                tr.innerHTML = "<td>" + m.name + "</td><td>" + m.email + "</td><td>" + (m.phone || "-") + "</td><td>" +
                    new Date(m.joinedAt).toLocaleDateString("en-AU") + "</td>";
                tbody.appendChild(tr);
            });
        });
    }

    /* ---------- events ---------- */

    $("coordinator-signin-form").addEventListener("submit", function (e) {
        e.preventDefault();
        api("POST", "/api/auth/coordinator-login", { password: $("password").value })
            .then(function () {
                state.signedIn = true;
                renderSession();
            })
            .catch(function (err) {
                $("signin-error").textContent = err.message;
                show($("signin-error"));
            });
    });

    $("signout-btn").addEventListener("click", function () {
        api("POST", "/api/auth/logout", {}).then(function () {
            state.signedIn = false;
            renderSession();
        });
    });

    $("round-select").addEventListener("change", function () {
        state.selectedRoundId = Number(this.value) || null;
        loadRoundData();
    });

    $("round-create").addEventListener("click", function () {
        var name = $("round-name").value.trim();
        var opens = $("round-opens").value;
        var closes = $("round-closes").value;
        if (!name || !opens || !closes) { alert("Round name, open and close times are required."); return; }
        api("POST", "/api/rounds", { name: name, opensAt: opens + ":00", closesAt: closes + ":00" })
            .then(function () {
                $("round-name").value = ""; $("round-opens").value = ""; $("round-closes").value = "";
                return loadRounds();
            })
            .then(loadRoundData)
            .catch(function (err) { alert(err.message); });
    });

    $("round-close").addEventListener("click", function () {
        if (!state.selectedRoundId) return;
        if (!confirm("Close this round? Members will no longer be able to change orders.")) return;
        api("POST", "/api/rounds/" + state.selectedRoundId + "/close", {})
            .then(loadRounds).then(loadRoundData)
            .catch(function (err) { alert(err.message); });
    });

    $("round-pack").addEventListener("click", function () {
        if (!state.selectedRoundId) return;
        api("POST", "/api/rounds/" + state.selectedRoundId + "/pack", {})
            .then(loadRounds).then(loadRoundData)
            .catch(function (err) { alert(err.message); });
    });

    $("print-summary").addEventListener("click", function () { window.print(); });

    $("show-inactive").addEventListener("change", loadProducts);

    $("product-create").addEventListener("click", function () {
        var payload = {
            name: $("product-name").value.trim(),
            description: $("product-desc").value.trim(),
            pricingType: $("product-type").value,
            price: $("product-price").value ? Number($("product-price").value) : null,
            active: true
        };
        api("POST", "/api/products", payload)
            .then(function () {
                $("product-name").value = ""; $("product-desc").value = ""; $("product-price").value = "";
                flash("product-msg", "Product added to the catalogue.", true);
                loadProducts();
            })
            .catch(function (err) { flash("product-msg", err.message, false); });
    });

    $("member-create").addEventListener("click", function () {
        var payload = {
            name: $("member-name").value.trim(),
            email: $("member-email").value.trim(),
            phone: $("member-phone").value.trim()
        };
        api("POST", "/api/members", payload)
            .then(function () {
                $("member-name").value = ""; $("member-email").value = ""; $("member-phone").value = "";
                flash("member-msg", "Member registered.", true);
                loadMembers();
            })
            .catch(function (err) { flash("member-msg", err.message, false); });
    });

    /* ---------- session ---------- */

    function renderSession() {
        if (state.signedIn) {
            $("whoami").textContent = "Coordinator mode";
            hide($("signin-panel"));
            show($("coordinator-panel"));
            show($("signout-btn"));
            loadRounds().then(loadRoundData);
            loadProducts();
            loadMembers();
        } else {
            $("whoami").textContent = "";
            show($("signin-panel"));
            hide($("coordinator-panel"));
            hide($("signout-btn"));
        }
    }

    api("GET", "/api/auth/me").then(function (me) {
        state.signedIn = (me.role === "coordinator");
        renderSession();
    });
})();
