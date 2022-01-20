package io.pleo.prop.objects

import io.pleo.prop.core.Prop
import javax.inject.Inject
import javax.inject.Named

@Suppress("unused")
class ComplexObjects @Inject constructor(
    @Named("io.pleo.test.prop1") val myComplexObjectProp: Prop<InjectedObject>,
    @Named("io.pleo.test.prop1") val withConstructor: Prop<InjectedObjectWithConstructor>,
    @Named("io.pleo.test.prop2") val myListOfComplexObjectProp: Prop<List<InjectedObject>>,
    @Named("io.pleo.test.prop3") val myStringProp: Prop<String>,
)
