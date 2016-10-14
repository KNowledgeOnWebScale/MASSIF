button = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "dul:hasDataValue": {
            "@type": "xsd:boolean"
        }
        , "dul:hasParameterDataValue": {
            "@type": "xsd:boolean"
        }
        , "ssn:hasValue": {
            "@type": "@id"
        }
        , "ssn:isProducedBy": {
            "@type": "@id"
        }
        , "ssniot": "http://IBCNServices.github.io/Accio-Ontology/SSNiot#"
    }
    , "@id": "ssniot:ButtonPushObservation@id@"
    , "@type": "ssniot:ButtonPushObservation"
    , "ssn:observedBy": {
        "@type": "ssniot:Button"
        , "@id": "ssniot:Button@id@"
        , "upper:hasID": "id"
    }
    , "ssn:observationResult": {
        "@type": "ssniot:ButtonPushSensorOutput"
        , "@id": "ssniot:ButtonPushSensorOutput@id@"
        , "ssn:hasValue": {
            "@type": "ssniot:PressureObservationValue"
            , "@id": "ssniot:PressureObservationValue@id@"
            , "dul:hasDataValue": "test"
            , "dul:hasParameterDataValue": "true"
        }
        , "ssn:isProducedBy": "ssniot:Button@id@"
    }
    , "ssn:observedProperty": {
        "@type": "ssniot:Pressure"
        , "@id": "ssniot:Pressure"
    }
};
temperature = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "dul:hasDataValue": {
            "@type": "xsd:float"
        }
        , "dul:hasParameterDataValue": {
            "@type": "xsd:boolean"
        }
        , "ssn:hasValue": {
            "@type": "@id"
        }
        , "ssn:isProducedBy": {
            "@type": "@id"
        }
        , "ssniot": "http://IBCNServices.github.io/Accio-Ontology/SSNiot#"
    }
    , "@id": "ssniot:TemperatureObservation@id@"
    , "@type": "ssniot:TemperatureObservation"
    , "ssn:observedBy": {
        "@type": "ssniot:TemperatureSensor"
        , "@id": "ssniot:TemperatureSensor@id@"
        , "upper:hasID": "id"
    }
    , "ssn:observationResult": {
        "@type": "ssniot:TemperatureSensorOutput"
        , "@id": "ssniot:TemperatureSensorOutput@id@"
        , "ssn:hasValue": {
            "@type": "ssniot:TemperatureObservationValue"
            , "@id": "ssniot:TemperatureObservationValue@id@"
            , "dul:hasDataValue": "test"
            , "dul:hasParameterDataValue": "true"
        }
        , "ssn:isProducedBy": "ssniot:TemperatureSensor@id@"
    }
    , "ssn:observedProperty": {
        "@type": "ssniot:Temperature"
        , "@id": "ssniot:Temperature@id@"
    }
};
contact = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "dul:hasDataValue": {
            "@type": "xsd:boolean"
        }
        , "dul:hasParameterDataValue": {
            "@type": "xsd:boolean"
        }
        , "ssn:hasValue": {
            "@type": "@id"
        }
        , "ssn:isProducedBy": {
            "@type": "@id"
        }
        , "ssniot": "http://IBCNServices.github.io/Accio-Ontology/SSNiot#"
    }
    , "@id": "ssniot:DoorWindowContactObservation@id@"
    , "@type": "ssniot:DoorWindowContactObservation"
    , "ssn:observedBy": {
        "@type": "ssniot:DoorWindowContactSensor"
        , "@id": "ssniot:DoorWindowContactSensor@id@"
        , "upper:hasID": "id"
    }
    , "ssn:observationResult": {
        "@type": "ssniot:DoorWindowContactSensorOutput"
        , "@id": "ssniot:DoorWindowContactSensorOutput@id@"
        , "ssn:hasValue": {
            "@type": "ssniot:DoorWindowContactObservationValue"
            , "@id": "ssniot:DoorWindowContactObservationValue@id@"
            , "dul:hasDataValue": "test"
            , "dul:hasParameterDataValue": "true"
        }
        , "ssn:isProducedBy": "ssniot:DoorWindowContactSensor@id@"
    }
    , "ssn:observedProperty": {
        "@type": "ssniot:DoorWindowContact"
        , "@id": "ssniot:DoorWindowContact@id@"
    }
};
lamp = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "dul:hasDataValue": {
            "@type": "xsd:boolean"
        }
        , "dul:hasParameterDataValue": {
            "@type": "xsd:boolean"
        }
        , "ssn:hasValue": {
            "@type": "@id"
        }
        , "ssn:isProducedBy": {
            "@type": "@id"
        }
        , "ssniot": "http://IBCNServices.github.io/Accio-Ontology/SSNiot#"
    }
    , "@id": "ssniot:LightIntensityObservation@id@"
    , "@type": "ssniot:LightIntensityObservation"
    , "ssn:observedBy": {
        "@type": "ssniot:LightSensor"
        , "@id": "ssniot:LightSensor@id@"
        , "upper:hasID": "id"
    }
    , "ssn:observationResult": {
        "@type": "ssniot:LightSensorOutput"
        , "@id": "ssniot:LightSensorOutput@id@"
        , "ssn:hasValue": {
            "@type": "ssniot:LightIntensityObservationValue"
            , "@id": "ssniot:LightIntensityObservationValue@id@"
            , "dul:hasDataValue": "test"
            , "dul:hasParameterDataValue": "true"
        }
        , "ssn:isProducedBy": "ssniot:LightSensor@id@"
    }
    , "ssn:observedProperty": {
        "@type": "ssniot:LightIntensity"
        , "@id": "ssniot:LightIntensity@id@"
    }
};
lock = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "dul:hasDataValue": {
            "@type": "xsd:boolean"
        }
        , "dul:hasParameterDataValue": {
            "@type": "xsd:boolean"
        }
        , "ssn:hasValue": {
            "@type": "@id"
        }
        , "ssn:isProducedBy": {
            "@type": "@id"
        }
        , "ssniot": "http://IBCNServices.github.io/Accio-Ontology/SSNiot#"
    }
    , "@id": "ssniot:LockObservation"
    , "@type": "ssniot:LockObservation"
    , "ssn:observedBy": {
        "@type": "ssniot:LockSensor"
        , "@id": "ssniot:LockSensor"
        , "upper:hasID": "id"
    }
    , "ssn:observationResult": {
        "@type": "ssniot:LockSensorOutput"
        , "@id": "ssniot:LockSensorOutput@id@"
        , "ssn:hasValue": {
            "@type": "ssniot:LockObservationValue"
            , "@id": "ssniot:LockObservationValue@id@"
            , "dul:hasDataValue": "test"
            , "dul:hasParameterDataValue": "true"
        }
        , "ssn:isProducedBy": "ssniot:LockSensor@id@"
    }
    , "ssn:observedProperty": {
        "@type": "ssniot:Lock"
        , "@id": "ssniot:Lock@id@"
    }
};
person = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "dul:hasDataValue": {
            "@type": "xsd:string"
        }
        , "dul:hasParameterDataValue": {
            "@type": "xsd:boolean"
        }
        , "ssn:hasValue": {
            "@type": "@id"
        }
        , "ssn:isProducedBy": {
            "@type": "@id"
        }
        , "ssniot": "http://IBCNServices.github.io/Accio-Ontology/SSNiot#"
    }
    , "@id": "ssniot:RFIDTagObservation@id@"
    , "@type": "ssniot:RFIDTagObservation"
    , "ssn:observedBy": {
        "@type": "ssniot:RFIDSensor"
        , "@id": "ssniot:RFIDSensor@id@"
        , "upper:hasID": "id"
    }
    , "ssn:observationResult": {
        "@type": "ssniot:RFIDSensorOutput"
        , "@id": "ssniot:RFIDSensorOutput@id@"
        , "ssn:hasValue": {
            "@type": "ssniot:RFIDObservationValue"
            , "@id": "ssniot:RFIDObservationValue@id@"
            , "dul:hasDataValue": "test"
            , "dul:hasParameterDataValue": "true"
        }
        , "ssn:isProducedBy": "ssniot:RFIDSensor@id@"
    }
    , "ssn:observedProperty": {
        "@type": "ssniot:RFIDTag"
        , "@id": "ssniot:RFIDTag@id@"
    }
};
windowObj = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "dul:hasDataValue": {
            "@type": "xsd:string"
        }
        , "dul:hasParameterDataValue": {
            "@type": "xsd:boolean"
        }
        , "ssn:hasValue": {
            "@type": "@id"
        }
        , "ssn:isProducedBy": {
            "@type": "@id"
        }
        , "ssniot": "http://IBCNServices.github.io/Accio-Ontology/SSNiot#"
    }
    , "@id": "ssniot:WindowObservation@id@"
    , "@type": "ssniot:WindowObservation"
    , "ssn:observedBy": {
        "@type": "ssniot:WindowSensor"
        , "@id": "ssniot:WindowSensor@id@"
        , "upper:hasID": "id"
    }
    , "ssn:observationResult": {
        "@type": "ssniot:WindowSensorOutput"
        , "@id": "ssniot:WindowSensorOutput@id@"
        , "ssn:hasValue": {
            "@type": "ssniot:WindowObservationValue"
            , "@id": "ssniot:WindowObservationValue@id@"
            , "dul:hasDataValue": "test"
            , "dul:hasParameterDataValue": "true"
        }
        , "ssn:isProducedBy": "ssniot:WindowSensor@id@"
    }
    , "ssn:observedProperty": {
        "@type": "ssniot:Window"
        , "@id": "ssniot:Window@id@"
    }
};
color = {
    "@context": {
        "ssn": "http://IBCNServices.github.io/Accio-Ontology/ontologies/ssn#"
        , "upper": "http://IBCNServices.github.io/Accio-Ontology/UpperAccio.owl#"
        , "dul": "http://IBCNServices.github.io/Accio-Ontology/ontologies/DUL.owl#"
        , "xsd": "http://www.w3.org/2001/XMLSchema#"
        , "demo": "http://IBCNServices.github.io/Accio-Ontology/demo/sensors.owl#"
        , "dul:hasDataValue": {
            "@type": "xsd:string"
        }
    }
    , "@id": "demo:ColorPreferenceEmpl@id@"
    , "@type": "demo:LightColorPreference"
    , "dul:hasDataValue": "@color@"
};