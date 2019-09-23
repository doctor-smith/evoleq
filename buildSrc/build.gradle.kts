plugins{
    `kotlin-dsl`
}
repositories{
    jcenter()
}

configure<JavaPluginConvention> {
    sourceSets{
        getByName("main"){
            resources.srcDirs("src/main/resources")
        }
    }
}