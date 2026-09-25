plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra
val codecui_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")
    // JiJ'd into Moonlight, so not on the dev runtime classpath, so add it explicitly
    modRuntimeOnly("net.mehvahdjukaar:codecui-fabric:${codecui_version}")

    modCompileOnly("com.terraformersmc:modmenu:4.0.6") {
        exclude(module = "fabric-api")
    }
}
