package io.springframework.dataflowacceptancetests.ci

import io.springframework.common.job.BuildAndDeploy
import io.springframework.common.job.Cron
import io.springframework.common.job.JdkConfig
import io.springframework.common.job.Maven
import io.springframework.common.job.TestPublisher
import javaposse.jobdsl.dsl.DslFactory
/**
 * @author Soby Chacko
 */
class ScdfAcceptanceTestsBuildMaker implements JdkConfig, TestPublisher,
        Cron, Maven, BuildAndDeploy {

    private final DslFactory dsl
    final String organization
    final String project
    String branchToBuild = "master"
    boolean ghPushTrigger = true

    ScdfAcceptanceTestsBuildMaker(DslFactory dsl, String organization, String project) {
        this.dsl = dsl
        this.organization = organization
        this.project = project
    }

    @Override
    String projectSuffix() {
        return 'spring-cloud-dataflow-acceptance-tests'
    }

    static String scriptToExecute(String script) {
        return """
						echo "Running script"
						bash ${script}
					"""
    }

    void deploy(String ciName, String script) {
        dsl.job("${prefixJob(project)}-${ciName}-ci") {
            if (ghPushTrigger) {
                triggers {
                    githubPush()
                }
            }
            jdk jdk8()
            wrappers {
                colorizeOutput()
                timeout {
                    noActivity(300)
                    failBuild()
                    writeDescription('Build failed due to timeout after {0} minutes of inactivity')
                }
            }
            scm {
                git {
                    remote {
                        url "https://github.com/${organization}/${project}"
                        branch branchToBuild

                    }
                }
            }
            steps {
                if (script != null) {
                    shell(scriptToExecute(script))
                }
            }
            configure {


            }
            publishers {
                mailer('schacko@pivotal.io', true, true)

            }
        }
    }
}
