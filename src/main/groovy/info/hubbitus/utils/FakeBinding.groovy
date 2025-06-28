package info.hubbitus.utils

/**
* Class to process groovy simple template and leave unbounded placeholders as is.
* Idea from https://stackoverflow.com/questions/44025138/how-to-ignore-missing-parameters-in-groovys-template-engine/75997572#75997572
**/
class FakeBinding {
    private def value

    FakeBinding(x) {
        value = x
    }

    def propertyMissing(x) {
        return new FakeBinding(value + '.' + x)
    }

    String toString() {
        return '$' + value
    }
}
