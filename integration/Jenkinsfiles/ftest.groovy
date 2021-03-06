/*
 * (C) Copyright 2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     jcarsique
 */

currentBuild.setDescription("Branch: $BRANCH -> $PARENT_BRANCH, DB: $DBPROFILE, VERSION: $DBVERSION")

node('SLAVE') {
    tool name: 'ant-1.9', type: 'ant'
    tool name: 'java-8-openjdk', type: 'hudson.model.JDK'
    tool name: 'maven-3', type: 'hudson.tasks.Maven$MavenInstallation'
    timeout(time: 3, unit: 'HOURS') {
        timestamps {
            def sha
            def shared
            stage('clone') {
                checkout(
                    [$class: 'GitSCM',
                        branches: [[name: '*/${BRANCH}']],
                        browser: [$class: 'GithubWeb', repoUrl: 'https://github.com/nuxeo/nuxeo'],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'PathRestriction', excludedRegions: '', includedRegions: '''nuxeo-distribution/.*
integration/.*'''],
                            [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: 'pom.xml'], [path: 'nuxeo-distribution'], [path: 'integration']]],
                            [$class: 'WipeWorkspace'],
                            [$class: 'CleanBeforeCheckout'],
                            [$class: 'CloneOption', depth: 5, noTags: true, reference: '', shallow: true]
                        ],
                        submoduleCfg: [],
                        userRemoteConfigs: [[url: 'git://github.com/nuxeo/nuxeo.git']]
                    ])
                shared = load("$WORKSPACE/integration/Jenkinsfiles/shared.groovy")
                sha = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                stash 'clone'
            }
            try {
                parallel (
                    "cmis" : {
                        node('SLAVE') {
                            stage('cmis') {
                                ws("$WORKSPACE-cmis") {
                                    unstash "clone"
                                    timeout(time: 2, unit: 'HOURS') {
                                        shared.withBuildStatus("$DBPROFILE-$DBVERSION/ftest/cmis", 'https://github.com/nuxeo/nuxeo', sha) {
                                            shared.withDockerCompose("$JOB_NAME-$BUILD_NUMBER-cmis", "integration/Jenkinsfiles/docker-compose-$DBPROFILE-${DBVERSION}.yml", "mvn -B -f $WORKSPACE/nuxeo-distribution/nuxeo-server-cmis-tests/pom.xml clean verify -Pqa,tomcat,$DBPROFILE") {
                                                archive 'nuxeo-distribution/nuxeo-server-cmis-tests/target/**/failsafe-reports/*, nuxeo-distribution/nuxeo-server-cmis-tests/target/*.png, nuxeo-distribution/nuxeo-server-cmis-tests/target/*.json, nuxeo-distribution/nuxeo-server-cmis-tests/target/**/*.log, nuxeo-distribution/nuxeo-server-cmis-tests/target/**/log/*, nuxeo-distribution/nuxeo-server-cmis-tests/target/**/nxserver/config/distribution.properties, nuxeo-distribution/nuxeo-server-cmis-tests/target/nxtools-reports/*'
                                                shared.failOnServerError('nuxeo-distribution/nuxeo-server-cmis-tests/target/tomcat/log/server.log')
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "funkload" : {
                        node('SLAVE') {
                            stage('funkload') {
                                ws("$WORKSPACE-funkload") {
                                    unstash "clone"
                                    timeout(time: 2, unit: 'HOURS') {
                                        shared.withBuildStatus("$DBPROFILE-$DBVERSION/ftest/funkload", 'https://github.com/nuxeo/nuxeo', sha) {
                                            shared.withDockerCompose("$JOB_NAME-$BUILD_NUMBER-funkload", "integration/Jenkinsfiles/docker-compose-$DBPROFILE-${DBVERSION}.yml", "mvn -B -f $WORKSPACE/nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/pom.xml clean verify -Pqa,tomcat,$DBPROFILE") {
                                                archive 'nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/target/**/failsafe-reports/*, nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/target/*.png, nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/target/*.json, nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/target/**/*.log, nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/target/**/log/*, nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/target/**/nxserver/config/distribution.properties, nuxeo-distribution/nuxeo-jsf-ui-funkload-tests/target/results/*/*'
                                                shared.failOnServerError('nuxeo-distribution/nuxeo-server-funkload-tests/target/tomcat/log/server.log')
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "webdriver" : {
                        node('SLAVE') {
                            stage('webdriver') {
                                ws("$WORKSPACE-webdriver") {
                                    unstash "clone"
                                    timeout(time: 2, unit: 'HOURS') {
                                        shared.withBuildStatus("$DBPROFILE-$DBVERSION/ftest/webdriver", 'https://github.com/nuxeo/nuxeo', sha) {
                                            shared.withDockerCompose("$JOB_NAME-$BUILD_NUMBER-webdriver", "integration/Jenkinsfiles/docker-compose-$DBPROFILE-${DBVERSION}.yml", "mvn -B -f $WORKSPACE/nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/pom.xml clean verify -Pqa,tomcat,$DBPROFILE") {
                                                archive 'nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/**/failsafe-reports/*, nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/*.png, nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/*.json, nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/**/*.log, nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/**/log/*, nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/**/nxserver/config/distribution.properties, nuxeo-distribution/nuxeo-server-cmis-tests/target/nxtools-reports/*, nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/results/*/*'
                                                junit '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml, **/target/failsafe-reports/**/*.xml'
                                                shared.failOnServerError('nuxeo-distribution/nuxeo-jsf-ui-webdriver-tests/target/tomcat/log/server.log')
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            } finally {
                shared.warningsPublisher()
                shared.claimPublisher()
            }
        }
    }
}

