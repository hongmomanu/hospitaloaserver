

db.createUser(
  {
    user: "jack",
    pwd: "1313",
    roles:
    [
      {
        role: "userAdminAnyDatabase",
        db: "admin"
      }
    ]
  }
)


db.runCommand(
  {
    usersInfo:"jack",
    showPrivileges:true
  }
)






use hospitaloaapp


mongo  111.1.76.108/hospitaloaapp -u jack -p





db.createUser( { "user" : "jack",
                 "pwd": "1313",

                 "roles" : [ { role: "clusterAdmin", db: "admin" },
                             { role: "readAnyDatabase", db: "admin" },
                             "readWrite"
                             ] },
               { w: "majority" , wtimeout: 5000 } )


show dbs
show collections








--用户表

db.users.insert({
username : "jack",
realname : "王小明",
deptid:"",
password:"1"
})

--科室表
db.depts.insert({
deptname : "jack"
})


--消息表
db.messages.insert({
content : "hello",
fromid:"",
toid:"",
groupid:"",
isread:false,
ftype:'text',
mtype:"group"
})


















