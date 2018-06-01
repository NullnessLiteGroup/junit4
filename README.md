# JUnit 4
JUnit is a simple framework to write repeatable tests. It is an instance of the xUnit architecture for unit testing frameworks.

For more information, please visit:
* [Wiki](https://github.com/junit-team/junit4/wiki)
* [Download and Install guide](https://github.com/junit-team/junit4/wiki/Download-and-Install)
* [Getting Started](https://github.com/junit-team/junit4/wiki/Getting-started)

[![Latest Build Status](https://junit.ci.cloudbees.com/job/JUnit/badge/icon)](https://junit.ci.cloudbees.com/)

[![Built on DEV@cloud](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png)](http://www.cloudbees.com/foss/foss-dev.cb)

# Note From NullnessLiteGroup
We used the forked JUnit4 source code to evaluate our project: [Nullness_Lite](https://github.com/weifanjiang/Nullness_Lite)

## Branches we created for evaluation
| The Checker Being Evaluated | Branch Name |
|--|--|
| The Nullness_Lite Option | annos_nl_all |
| 1) All variables initialized | annos_nl_init |
| 2) No aliasing + all methods @SideEffectsFree | annos_nl_inva |
| 3) Map.get() returns @NonNull | annos_nl_mapk |
| 4) BoxedClass.valueOf() are @Pure | annos_nl_boxp |
| The Nullness Checker | annos_nc_all |
| IntelliJ | intellij1 |
| IntelliJ with Infer Nullity | intellij2 |
| Eclipse | eclipse |
| FindBugs | findbugs |
| NullAway with annotations added by Infer Nullity | Nullaway_Intellij |
| NullAway with annotations required for the Nullness Checker | Nullaway_nc |
| NullAway with annotations required for NullnessLite | Nullaway_nl |

## Edits in the Source Files
We only added additional Comments that provides infomation for our evaluation process. For more infomation, please look at the Evaluation section in our project's [report](https://github.com/weifanjiang/Nullness_Lite/blob/master/reports/week10/Report-10.pdf).
