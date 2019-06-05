`curl -H "Content-Type: application/json" -d "{ \"dimension\": 15 }" -X POST http://localhost:11001/control/result-simple`



fixing bugs.
writing documentation.

Nodes are stared with:

replace bootstrap address with real one.

-Dserver.address=0.0.0.0 -Dserver.port=11001 -Dbootstrap.addr=0.0.0.0 -Dbootstrap.port=10000