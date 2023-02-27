grammar Curl;

@header{
    package expression;
}

curl
    : 'curl' '--location' '--request' method=('GET'|'POST'|'PUT'|'DELETE') URL (HEADER+)? (DATA)?  # CURLCOMMAND
    ;

DATA: ('--data' | '--data-raw') WS '\''STRINGVALUE'\'';

URL: '\''STRINGVALUE'\'';

HEADER
    : '--header' WS '\''STRINGVALUE':'STRINGVALUE'\''
    | '--header' WS '\''STRINGVALUE';'WS?'\'';

fragment STRINGVALUE: ((JAPANIES | ALPHANUMERIC | SPECIALCHARACTER |WS)+)?;
fragment ALPHANUMERIC: [a-zA-Z0-9_-];
fragment SPECIALCHARACTER: [[\]?=(){}.%:!/@|,&;#$"”*~+ `∫];
fragment JAPANIES: [\u3000-\uff9f];
WS: [ \n\t\r\\] + -> skip;