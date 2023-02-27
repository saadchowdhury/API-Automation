package model

import com.xlson.groovycsv.PropertyMapper
import groovy.transform.builder.Builder

@Builder
class CsvTestRow {
    PropertyMapper row
    def headers
}
