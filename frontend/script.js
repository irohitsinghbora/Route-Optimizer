const BASE_URL = "http://localhost:8080/api";

let locationMap = {};

// Load locations on start
window.onload = function () {
    fetchLocations();
};

function addLocation() {
    const name = document.getElementById("locationName").value;

    fetch(`${BASE_URL}/locations`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ name })
    })
        .then(() => {
            alert("Location added!");
            fetchLocations();
        });
}

function addRoute() {
    const sourceName = document.getElementById("src").value.trim();
    const destName = document.getElementById("dst").value.trim();
    const distance = document.getElementById("dist").value;

    const sourceId = Object.keys(locationMap)
        .find(id => locationMap[id].toLowerCase() === sourceName.toLowerCase());

    const destinationId = Object.keys(locationMap)
        .find(id => locationMap[id].toLowerCase() === destName.toLowerCase());

    if (!sourceId || !destinationId) {
        alert("Location not found!");
        return;
    }

    fetch(`${BASE_URL}/routes`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            sourceId,
            destinationId,
            distance
        })
    })
        .then(() => alert("Route added!"));
}

function fetchLocations() {
    fetch(`${BASE_URL}/locations`)
        .then(res => res.json())
        .then(data => {
            locationMap = {};
            data.forEach(loc => {
                locationMap[loc.id] = loc.name;
            });
        });
}

function getPaths() {
    const sourceName = document.getElementById("source").value.trim();
    const destName = document.getElementById("dest").value.trim();

    const source = Object.keys(locationMap)
        .find(id => locationMap[id].toLowerCase() === sourceName.toLowerCase());

    const dest = Object.keys(locationMap)
        .find(id => locationMap[id].toLowerCase() === destName.toLowerCase());

    if (!source || !dest) {
        alert("Location not found!");
        return;
    }

    fetch(`${BASE_URL}/all-paths?source=${source}&dest=${dest}`)
        .then(res => res.json())
        .then(data => {
            displayPaths(data);
        });
}

function displayPaths(data) {
    const container = document.getElementById("paths");
    container.innerHTML = "";

    const allPaths = data.allPaths;
    const shortest = JSON.stringify(data.shortestPath);

    // Show shortest distance
    const distanceDiv = document.createElement("h3");
    distanceDiv.innerText = "Shortest Distance: " + data.shortestDistance;
    container.appendChild(distanceDiv);

    allPaths.forEach(path => {
        const div = document.createElement("div");
        div.classList.add("path");

        if (JSON.stringify(path) === shortest) {
            div.classList.add("shortest");
        }

        const namedPath = path.map(id => locationMap[id] || id);

        // FIXED ARROW
        div.innerHTML = namedPath.join(" &rarr; ");

        container.appendChild(div);
    });
}