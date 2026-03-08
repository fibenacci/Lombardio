package main

import (
    "fmt"
)

type Customer struct {
    ID string "json:id"
    CustomerNumber "json:customNumber"
    Name string "json:name"
    Surname string
    Address Address
}

