#!/usr/bin/env bash

curl -i -X POST http://localhost:8080/customers -H 'Content-Type: application/json' -d '{"name":"Oleg","type":"regular"}'

curl -i -X POST http://localhost:8080/customers -H 'Content-Type: application/json' -d '{"name":"Anna","type":"premium"}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Stels"}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Trek"}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Forward"}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Giant"}'

curl -i -X GET http://localhost:8080/customers

curl -i -X GET http://localhost:8080/customers/1

curl -i -X GET 'http://localhost:8080/customers?type=regular'

curl -i -X GET 'http://localhost:8080/customers?name=Anna'

curl -i -X GET 'http://localhost:8080/customers?name=Anna&type=premium'

curl -i -X PUT http://localhost:8080/customers/1 -H 'Content-Type: application/json' -d '{"name":"Ivan","type":"regular"}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":1}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":2}'

curl -i -X POST http://localhost:8080/customers/2/rentals -H 'Content-Type: application/json' -d '{"bikeId":1}'

curl -i -X DELETE http://localhost:8080/bikes/1

curl -i -X DELETE http://localhost:8080/customers/1

curl -i -X POST http://localhost:8080/customers/2/returns -H 'Content-Type: application/json' -d '{"bikeId":1}'

curl -i -X POST http://localhost:8080/customers/2/rentals -H 'Content-Type: application/json' -d '{"bikeId":2}'

curl -i -X POST http://localhost:8080/customers/2/rentals -H 'Content-Type: application/json' -d '{"bikeId":3}'

curl -i -X POST http://localhost:8080/customers/2/rentals -H 'Content-Type: application/json' -d '{"bikeId":4}'

curl -i -X POST http://localhost:8080/bikes -H 'Content-Type: application/json' -d '{"model":"Extra"}'

curl -i -X POST http://localhost:8080/customers/2/rentals -H 'Content-Type: application/json' -d '{"bikeId":5}'

curl -i -X PUT http://localhost:8080/customers/2 -H 'Content-Type: application/json' -d '{"name":"Changed","type":"regular"}'

curl -i -X GET http://localhost:8080/customers/2

curl -i -X GET 'http://localhost:8080/bikes?available=false'

curl -i -X POST http://localhost:8080/customers/1/returns -H 'Content-Type: application/json' -d '{"bikeId":1}'

curl -i -X POST http://localhost:8080/customers/1/returns -H 'Content-Type: application/json' -d '{"bikeId":1}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":5}'

curl -i -X POST http://localhost:8080/customers/1/returns -H 'Content-Type: application/json' -d '{"bikeId":5}'

curl -i -X POST http://localhost:8080/customers/2/returns -H 'Content-Type: application/json' -d '{"bikeId":2}'

curl -i -X POST http://localhost:8080/customers/2/returns -H 'Content-Type: application/json' -d '{"bikeId":3}'

curl -i -X PUT http://localhost:8080/customers/2 -H 'Content-Type: application/json' -d '{"name":"Anna","type":"regular"}'

curl -i -X POST http://localhost:8080/customers/2/rentals -H 'Content-Type: application/json' -d '{"bikeId":2}'

curl -i -X POST http://localhost:8080/customers/2/returns -H 'Content-Type: application/json' -d '{"bikeId":4}'

curl -i -X POST http://localhost:8080/customers -H 'Content-Type: application/json' -d '{"name":"","type":"regular"}'

curl -i -X POST http://localhost:8080/customers -H 'Content-Type: application/json' -d '{"name":123,"type":"regular"}'

curl -i -X POST http://localhost:8080/customers -H 'Content-Type: application/json' -d '{"name":"X","type":"vip"}'

curl -i -X POST http://localhost:8080/customers -H 'Content-Type: application/json' -d '{}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":0}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":-1}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":1.5}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":"1"}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":null}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":true}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":9223372036854775808}'

curl -i -X POST http://localhost:8080/customers/999/rentals -H 'Content-Type: application/json' -d '{"bikeId":1}'

curl -i -X POST http://localhost:8080/customers/1/rentals -H 'Content-Type: application/json' -d '{"bikeId":999}'

curl -i -X GET http://localhost:8080/customers/1/rentals

curl -i -X GET http://localhost:8080/customers/1/extra

curl -i -X GET 'http://localhost:8080/customers?type=vip'

curl -i -X GET 'http://localhost:8080/customers?name=Anna&name=Ivan'

curl -i -X GET http://localhost:8080/customers/abc

curl -i -X GET http://localhost:8080/customers/999

curl -i -X PUT http://localhost:8080/customers/999 -H 'Content-Type: application/json' -d '{"name":"X","type":"regular"}'

curl -i -X PATCH http://localhost:8080/customers/1

curl -i -X DELETE http://localhost:8080/customers/1

curl -i -X DELETE http://localhost:8080/customers/2

curl -i -X DELETE http://localhost:8080/customers/1

curl -i -X DELETE http://localhost:8080/bikes/1

curl -i -X DELETE http://localhost:8080/bikes/2

curl -i -X DELETE http://localhost:8080/bikes/3

curl -i -X DELETE http://localhost:8080/bikes/4

curl -i -X DELETE http://localhost:8080/bikes/5

curl -i -X GET http://localhost:8080/customers

curl -i -X GET http://localhost:8080/bikes
