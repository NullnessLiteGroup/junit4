# Evaluation Script for Checking JUnit4 by the Nullness Checker
#
# JUnit4 original URL: https://github.com/junit-team/junit4
#
# This script counts the annotations used, and the analyasis report for the current branch
#
# Author(s): XINRONG ZHAO

# Checkout the branch
echo "Current branch is"$(git branch | grep \* | sed -r "s/\*//g")

# Analysis the report
echo
echo "# of Annotations used:"
echo "\t@Nullable\t\t"$(find src/main | grep -e "\.java$" | xargs grep -on "@Nullable" | wc -l)
echo
echo "Analysis Report"
echo "\tTrue Positives\t\t"$(find src/main | grep -e "\.java$" | xargs grep -on "TRUE_POSITIVE" | wc -l)
echo "\tFalse Positives\t\t"$(find src/main | grep -e "\.java$" | xargs grep -on "FALSE_POSITIVE" | wc -l)
