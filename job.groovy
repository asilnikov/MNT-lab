def giturl = 'https://github.com/asilnikov/MNT-lab.git'
job ("MNTLAB-silnikov-main-build-job") {

    parameters{
        choiceParam('BRANCH_NAME', ['asilnikov', 'master'], 'Branch name')
        activeChoiceParam('steps_job') {
            description('Choose job')
            choiceType('CHECKBOX')
            filterable(false)
            groovyScript {
                script('''return ["MNTLAB-asilnikov-child1-build-job", "MNTLAB-asilnikov-child2-build-job", "MNTLAB-asilnikov-child3-build-job", "MNTLAB-asilnikov-child4-build-job"]''')
            }
        }
    }

    steps {
        downstreamParameterized {
            trigger('$steps_job') {
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

for (i in (1..4)) {
    job("MNTLAB-asilnikov-child${i}-build-job") {

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
            shell('chmod +x ./script.sh; ./script.sh > output.txt; tar -czf ${BRANCH_NAME}_dsl_script.tar.gz job.groovy')
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

