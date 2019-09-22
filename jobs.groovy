def giturl = 'https://github.com/asilnikov/MNT-lab.git'
job ("MNTLAB-Additional_task-main_Additional-build-job") {

    parameters{
        choiceParam('BRANCH_NAME', ['Additional_task', 'master'], 'Branch name')
        activeChoiceParam('steps_job') {
            description('Choose job')
            choiceType('CHECKBOX')
            filterable(false)
            groovyScript {
                script('''return ["MNTLAB-Additional_task-Additional2-build-job", 
                                  "MNTLAB-Additional_task-Additional1-build-job"]''')
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

for (i in (1..2)) {
    job("MNTLAB-Additional_task-Additional${i}-build-job") {

        scm {
            git {
                remote {
                    url(giturl)
                }
                branch('Additional_task')
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
            shell('java -jar *.jar > Additional_task.txt; tar -czf ${BRANCH_NAME}_Additional_task.tar.gz Additional_task.txt')
        }
        publishers {
            archiveArtifacts {
                pattern('${BRANCH_NAME}_Additional_task.tar.gz')
                allowEmpty(false)
                onlyIfSuccessful(false)
                fingerprint(false)
                defaultExcludes(true)
            }
        }
    }
}
