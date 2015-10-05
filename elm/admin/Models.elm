module Models where 


type Action =
  NoOp
  | LoadUsers UserList


type alias User = {
  id : Int,
  name : String,
  email : String
}


type alias UserList = (List User)


type alias Model = {
  users : UserList
}