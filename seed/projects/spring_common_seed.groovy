job('spring-common-seed') {
    triggers {
        githubPush()
    }
    scm {
        git {
            remote {
                github('spring-io/build-scripts')
            }
            branch('master')
        }
    }
    steps {
        gradle("clean build")
        dsl {
            external('jobs/springcommon/*.groovy')
            removeAction('IGNORE')
            removeViewAction('IGNORE')
            ignoreExisting(false)
            additionalClasspath([
                    'src/main/groovy', 'src/main/resources', 'build/lib/*.jar'
            ].join("\n"))
        }
    }
}
