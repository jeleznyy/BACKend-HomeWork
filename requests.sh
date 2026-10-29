#!/usr/bin/env bash

curl -i http://localhost:8080/ping

curl -i -X POST http://localhost:8080/ping

curl -i http://localhost:8080/ping/extra

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Stels"}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Forward"}'

curl -i http://localhost:8080/bikes

curl -i http://localhost:8080/bikes/1

curl -i 'http://localhost:8080/bikes?model=Stels'

curl -i 'http://localhost:8080/bikes?available=true'

curl -i 'http://localhost:8080/bikes?available=false'

curl -i 'http://localhost:8080/bikes?model=Stels&available=true'

curl -i -X PUT http://localhost:8080/bikes/1 -H 'Content-Type: application/json' -d '{"model":"Trek"}'

curl -i http://localhost:8080/bikes/1

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":" "}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":123}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{bad json}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d 'null'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Stels","extra":1}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Stels"} {}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Stels","model":"Trek"}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d ''

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: text/plain' -d '{"model":"Stels"}'

curl -i http://localhost:8080/bikes/999

curl -i http://localhost:8080/bikes/abc

curl -i http://localhost:8080/bikes/0

curl -i 'http://localhost:8080/bikes?available=yes'

curl -i 'http://localhost:8080/bikes?color=red'

curl -i 'http://localhost:8080/bikes?model=Stels&model=Trek'

curl -i -X PATCH http://localhost:8080/bikes/1

curl -i -X PUT http://localhost:8080/bikes

curl -i http://localhost:8080/bikes/1/extra

curl -i -X PUT http://localhost:8080/bikes/999 -H 'Content-Type: application/json' -d '{"model":"Trek"}'

curl -i -X PUT http://localhost:8080/bikes/1 -H 'Content-Type: application/json' -d '{"model":" "}'

curl -i -X DELETE http://localhost:8080/bikes/1

curl -i http://localhost:8080/bikes/1

curl -i -X DELETE http://localhost:8080/bikes/1

curl -i -X DELETE http://localhost:8080/bikes/2

curl -i http://localhost:8080/bikes
