cat $1| grep Greeting | sed 's/.*classID: \(.*\), jvm.*/\1/'  |sort -u | wc -l
