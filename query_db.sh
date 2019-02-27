#!/bin/bash
docker exec -it db psql -U vote -d vote -c "$@"
