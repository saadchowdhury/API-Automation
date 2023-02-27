grammar Validation;

@header{
    package expression;
}

validationStatement: (multipleStatement SEPARATOR?)*;

existType:              Exist keyString                                                 #existKey;
dataTypeType:           Datatype keyString keyString                                    #datatypekey;
equalType:              keyString '==' Identifier                                       #equalcheck;
responseType:           Identifier keyString '==' Identifier keyString                  #responseCheck;
dataList:               Exist keyList                                                   #existArrayKey;
dataTypeForList:        Datatype keyList keyString                                      #datatypeOfList;
combinationValidation: 'RESPONSE_LEFT' keyString '==' 'RESPONSE_RIGHT' keyString        #checkComb;
responseValidForTwoReq:'RESPONSE_LEFT' keyList '==' 'RESPONSE_RIGHT' keyList            #responseValidityForBrkt;
calculation:            keyString (Operator keyString)* '==' Identifier                 #calculateValue;
compare:                keyString CompareOperator Identifier                            #compareValue;
equalforList:           keyList '==' keyString                                          #equalForListCheck;
contain:                keyString Contains Identifier                                   #containCheck;
listContain:            keyList Contains Identifier                                     #containListCheck;
starts:                 keyString Startswith Identifier                                 #startsWithCheck;
startsList:             keyList Startswith Identifier                                   #startswithListCheck;
ends:                   keyString EndsWith Identifier                                   #endsWithCheck;
endsList:               keyList EndsWith Identifier                                     #endswithListCheck;
concatString:           Concat keyString ('&'keyString)* '==' Identifier                #concatMultipleString;
stringLength:           LengthOf keyString '==' Identifier                              #lengthOfString;
stringSize:             SizeOf keyString '==' Identifier                                #sizeOfString;
roudingOf:              roundOfWord keyString '==' Identifier                           #roundingOfValue;
minmax:                 extrema keyList '==' Identifier                                 #extremaOfValue;
dateFormat:             Date keyString Pattern Identifier (Identifier)*                 #checkDateFormat;
regex:                  'MATCH' keyString REGEX                                         #regexCheck;
sortMethod:             Sort keyString By keyString sortingVar                          #sortingMethod;


multipleStatement
    : (existType | dataList | dataTypeForList | dataTypeType | equalType
     | responseType | combinationValidation | responseValidForTwoReq | calculation
     | compare | equalforList | contain | listContain | starts | startsList | ends
     | endsList | concatString | stringLength | stringSize | roudingOf | minmax
     | dateFormat | regex | sortMethod)
    ;



Exist        : E X I S T;
Datatype     : D A T A T Y P E;
Contains     : C O N T A I N S;
Startswith   : S T A R T S W I T H;
EndsWith     : E N D S W I T H;
Concat       : C O N C A T;
LengthOf     : L E N G T H O F;
SizeOf       : S I Z E O F;
Floor        : F L O O R;
Ceil         : C E I L;
Round        : R O U N D;
Max          : M A X;
Min          : M I N;
Date         : D A T E;
Pattern      : P A T T E R N;
Asc          : A S C;
Desc         : D E S C;
Sort         : S O R T;
By           : B Y;

keyList
  : Identifier'[]'('.'Identifier)*?
  | Identifier'.'(Identifier'[]'('.'Identifier)*?)*?
  | Identifier'.'(Identifier'['keyString']'('.'Identifier)*?)*?
   ;
keyString
 : Identifier
 | Identifier ('.'Identifier)*?
  ;
roundOfWord
    : (Floor | Ceil | Round);

extrema:
    (Max | Min);

sortingVar
: (Asc | Desc)
;

REGEX
    :   'REGEX' (~[\n])+ ;

LogicalOperator
    : ('&&' | '||');

Operator
    : ('+' | '-' | '*' | '/');

Identifier
 : [a-zA-Z_0-9] [a-zA-Z_0-9\-:]*
 ;

CompareOperator:
    ('>' | '<' | '=' | '<=' | '>=' | '!=');




// Alphabet
fragment E : 'e' | 'E';
fragment I : 'i' | 'I';
fragment S : 's' | 'S';
fragment X : 'x' | 'X';
fragment T : 't' | 'T';
fragment D : 'd' | 'D';
fragment A : 'a' | 'A';
fragment Y : 'y' | 'Y';
fragment P : 'p' | 'P';
fragment C : 'c' | 'C';
fragment O : 'o' | 'O';
fragment N : 'n' | 'N';
fragment R : 'r' | 'R';
fragment W : 'w' | 'W';
fragment H : 'h' | 'H';
fragment L : 'l' | 'L';
fragment G : 'g' | 'G';
fragment F : 'f' | 'F';
fragment Z : 'z' | 'Z';
fragment U : 'u' | 'U';
fragment M : 'm' | 'M';
fragment B : 'b' | 'B';


WS: [ \t\r\\"] + -> skip;
SEPARATOR: [\n ,] + -> skip;