package constant

class Constants {
    final static String ARRAY(arrName){
        if( arrName == "ArrayList" || arrName == "Array" || arrName == "ArrayOfString" ) {
            return 'ArrayofString'
        }
    }

    final static String MAPTYPE(mapName){
        if( mapName == "Map" || mapName == "LazyMap") {
            return 'Map'
        }
    }

    final static String ARRAY_LIST(listName){
        if( listName == "ArrayList" || listName == "List") {
            return 'List'
        }
    }
}
