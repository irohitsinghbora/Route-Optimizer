const API = "http://localhost:8080/api";

// ---------------- LOGIN ----------------

async function login() {

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const res = await fetch(`${API}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username,
            password
        })
    });

    const data = await res.json();

    alert(data.message);

    if (data.message === "Login successful") {

        localStorage.setItem("loggedInUser", username);

        window.location.href = "locations.html";
    }
}

// ---------------- SIGNUP ----------------

async function signup() {

    const username = document.querySelectorAll("input")[0].value;
    const password = document.querySelectorAll("input")[1].value;

    const res = await fetch(`${API}/auth/signup`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username,
            password
        })
    });

    const data = await res.json();

    alert(data.message);

    if (data.message === "Signup successful") {
        window.location.href = "login.html";
    }
}

// ---------------- LOGOUT ----------------

function logout() {

    localStorage.removeItem("loggedInUser");

    window.location.href = "login.html";
}

// ---------------- CHECK LOGIN ----------------

function checkLogin() {

    const user = localStorage.getItem("loggedInUser");

    const currentPage = window.location.pathname;

    if (!user &&
        !currentPage.includes("login.html") &&
        !currentPage.includes("signup.html")) {

        window.location.href = "login.html";
    }
}

// ---------------- LOCATIONS ----------------

async function addLocation() {

    const input = document.getElementById("locationName");

    if (!input) return;

    const name = input.value.trim();

    if (!name) {
        alert("Enter location");
        return;
    }

    await fetch(`${API}/locations`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            name
        })
    });

    input.value = "";

    loadLocations();
    loadLocationsDropdown();
}

async function loadLocations() {

    const list = document.getElementById("locationsList");

    if (!list) return;

    const res = await fetch(`${API}/locations`);

    const data = await res.json();

    list.innerHTML = "";

    data.forEach(l => {

        list.innerHTML += `
            <div class="route-card">

                <div class="route-title">
                    ${l.name}
                </div>

                <button class="delete-btn"
                        onclick="deleteLocation(${l.id})">
                    Delete
                </button>

            </div>
        `;
    });
}

async function deleteLocation(id) {

    await fetch(`${API}/locations/${id}`, {
        method: "DELETE"
    });

    loadLocations();
    loadLocationsDropdown();
    loadRoutes();
}

// ---------------- DROPDOWNS ----------------

async function loadLocationsDropdown() {

    const source = document.getElementById("source");
    const destination = document.getElementById("destination");
    const dest = document.getElementById("dest");

    const res = await fetch(`${API}/locations`);

    const locations = await res.json();

    if (source && destination) {

        source.innerHTML = "";
        destination.innerHTML = "";

        locations.forEach(l => {

            source.innerHTML += `
                <option value="${l.id}">
                    ${l.name}
                </option>
            `;

            destination.innerHTML += `
                <option value="${l.id}">
                    ${l.name}
                </option>
            `;
        });
    }

    if (source && dest) {

        source.innerHTML = "";
        dest.innerHTML = "";

        locations.forEach(l => {

            source.innerHTML += `
                <option value="${l.name}">
                    ${l.name}
                </option>
            `;

            dest.innerHTML += `
                <option value="${l.name}">
                    ${l.name}
                </option>
            `;
        });
    }
}

// ---------------- ROUTES ----------------

async function addRoute() {

    const sourceId =
        Number(document.getElementById("source").value);

    const destinationId =
        Number(document.getElementById("destination").value);

    const distance =
        Number(document.getElementById("distance").value);

    if (!distance || distance <= 0) {

        alert("Invalid distance");
        return;
    }

    await fetch(`${API}/routes`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            sourceId,
            destinationId,
            distance
        })
    });

    document.getElementById("distance").value = "";

    loadRoutes();
}

async function loadRoutes() {

    const list = document.getElementById("routesList");

    if (!list) return;

    const routes =
        await (await fetch(`${API}/routes`)).json();

    const locations =
        await (await fetch(`${API}/locations`)).json();

    const map = {};

    locations.forEach(l => {
        map[l.id] = l.name;
    });

    list.innerHTML = "";

    routes.forEach(r => {

        list.innerHTML += `
            <div class="route-card">

                <div>

                    <div class="route-title">
                        ${map[r.sourceId]}
                        →
                        ${map[r.destinationId]}
                    </div>

                    <div class="route-distance">
                        Distance: ${r.distance}
                    </div>

                </div>

                <button class="delete-btn"
                        onclick="deleteRoute(${r.id})">
                    Delete
                </button>

            </div>
        `;
    });
}

async function deleteRoute(id) {

    await fetch(`${API}/routes/${id}`, {
        method: "DELETE"
    });

    loadRoutes();
}

// ---------------- FIND PATHS ----------------

async function getPaths() {

    const source =
        document.getElementById("source").value;

    const dest =
        document.getElementById("dest").value;

    const res =
        await fetch(
            `${API}/paths?sourceName=${source}&destinationName=${dest}`
        );

    const data = await res.json();

    const locations =
        await (await fetch(`${API}/locations`)).json();

    const map = {};

    locations.forEach(l => {
        map[l.id] = l.name;
    });

    // SHORTEST PATH

    const shortestPath =
        data.shortestPath.pathIds
            .map(id => map[id]);

    let shortestHTML = "";

    shortestPath.forEach((location, index) => {

        shortestHTML += `
            <div class="path-node">
                ${location}
            </div>
        `;

        if (index !== shortestPath.length - 1) {

            shortestHTML += `
                <div class="path-arrow">
                    →
                </div>
            `;
        }
    });

    // ALL PATHS

    let allPathsHTML = "";

    data.allPaths.forEach(pathObj => {

        const names =
            pathObj.path.map(id => map[id]);

        const isShortest =
            JSON.stringify(names) === JSON.stringify(shortestPath);

        let pathLine = "";

        names.forEach((location, index) => {

            pathLine += `
                <span class="${
                isShortest
                    ? 'mini-node active-mini-node'
                    : 'mini-node'
            }">
                    ${location}
                </span>
            `;

            if (index !== names.length - 1) {

                pathLine += `
                    <span class="mini-arrow">
                        →
                    </span>
                `;
            }
        });

        allPathsHTML += `
            <div class="${
            isShortest
                ? 'possible-path shortest-highlight'
                : 'possible-path'
        }">

                <div>
                    ${pathLine}
                </div>

                <div class="path-distance">
                    Distance: ${pathObj.distance}
                </div>

            </div>
        `;
    });

    document.getElementById("paths").innerHTML = `

        <div class="shortest-path-card">

            <h2>
                🚀 Shortest Path Found
            </h2>

            <div class="path-container">
                ${shortestHTML}
            </div>

            <div class="distance-box">
                Total Distance:
                <span>
                    ${data.shortestPath.distance}
                </span>
            </div>

        </div>

        <div class="all-paths-card">

            <h2>
                📍 All Possible Paths
            </h2>

            ${allPathsHTML}

        </div>
    `;
}

// ---------------- PAGE LOAD ----------------

window.onload = function () {

    checkLogin();

    loadLocations();

    loadLocationsDropdown();

    loadRoutes();
};