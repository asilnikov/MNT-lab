def giturl = 'https://github.com/asilnikov/MNT-lab.git'
job ("MNTLAB-silnikov-main_Additional-build-job") {

    parameters{
        choiceParam('BRANCH_NAME', ['asilnikov', 'master'], 'Branch name')
        activeChoiceParam('Next_job') {
            description('Choose job')
            choiceType('CHECKBOX')
            filterable(false)
            groovyScript {
                script('''return ["MNTLAB-asilnikov-Additional2-build-job", 
                                  "MNTLAB-asilnikov-Additional1-build-job"]''')
            }
        }
    }

    steps {
        downstreamParameterized {
            trigger('$Next_job') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
                }
            }
        }
    }
}

for (i in (1..2)) {
    job("MNTLAB-asilnikov-Additional${i}-build-job") {

        scm {
            git {
                remote {
                    url(giturl)
                }
                branch('$BRANCH_NAME')
            }
        }

        parameters {
            activeChoiceParam('BRANCH_NAME') {
                description('Choose branch')
                choiceType('SINGLE_SELECT')
                groovyScript {
                    script('''("git ls-remote -h https://github.com/asilnikov/MNT-lab.git").execute().text.readLines().collect {
                      it.split()[1].replaceAll(\'refs/heads/\', \'\')}.sort()''')
                }
            }
        }

        steps {
            shell('java -jar *.jar > output.txt; tar -czf ${BRANCH_NAME}_dsl_script.tar.gz jobs.groovy')
        }
        publishers {
            archiveArtifacts {
                pattern('${BRANCH_NAME}_dsl_script.tar.gz')
                allowEmpty(false)
                onlyIfSuccessful(false)
                fingerprint(false)
                defaultExcludes(true)
            }
        }
    }
}
