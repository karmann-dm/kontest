package com.karmanno.kontest.docker

class DockerContainer(
    val imageName: String
) {
    var ports: ArrayList<String> = ArrayList()
    var envs: HashMap<String, String> = HashMap()

    fun port(host: Int, container: Int): DockerContainer {
        ports.add("$host:$container")
        return this
    }

    fun env(envVariableMap: Map<String, String>): DockerContainer {
        envs += envVariableMap
        return this
    }

    fun env(key: String, value: String): DockerContainer {
        envs[key] = value
        return this
    }
}

data class DockerContainerInstance(
    val id: String,
    val container: DockerContainer
)