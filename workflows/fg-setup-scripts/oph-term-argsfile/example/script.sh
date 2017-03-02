#! /bin/bash
credentials=$(cat oph-credentials.txt)
user=${credentials%:*}
pass=${credentials#*:}

arguments=($(cat arguments.txt))
host=${arguments[0]}
port=${arguments[1]}
unset arguments[0]
unset arguments[1]

oph_term -u "$user" -p "$pass" -H "$host" -P "$port" -e "./workflow.json ${arguments[@]}" -j > output.json

curl https://raw.githubusercontent.com/tzok/ENES-portlet/develop/src/utils/ophidia_helper.py -o ophidia_helper.py
python2 ophidia_helper.py output.json 'Post (5)' oph-credentials.txt avg.png out.png

echo 'Finished!'
