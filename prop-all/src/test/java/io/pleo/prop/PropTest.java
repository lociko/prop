package io.pleo.prop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import org.junit.Before;
import org.junit.Test;

import io.pleo.prop.archaius.ArchaiusPropFactory;
import io.pleo.prop.guice.AutoPropModule;
import io.pleo.prop.guice.internal.FailedToCreatePropException;
import io.pleo.prop.guice.internal.JacksonParserFactory;
import io.pleo.prop.guice.internal.RequiredNamedAnnotationException;
import io.pleo.prop.objects.BothNamedAnnotations;
import io.pleo.prop.objects.ComplexObjects;
import io.pleo.prop.objects.EmptyNamedAnnotation;
import io.pleo.prop.objects.InjectedObject;
import io.pleo.prop.objects.InlineProviderModule;
import io.pleo.prop.objects.InvalidJSON;
import io.pleo.prop.objects.MyInterface;
import io.pleo.prop.objects.MyInterfaceProvider;
import io.pleo.prop.objects.NullValue;
import io.pleo.prop.objects.SamePropertyAsComplexObjects;
import io.pleo.prop.objects.UnnamedProp;
import io.pleo.prop.objects.UsesTwiceSameProp;

import static com.google.common.truth.Truth.*;

public class PropTest {

  @Before
  public void setup() {
  }

  @Test
  public void can_read_complex_properties() {
    Injector injector = createInjector(binder -> binder.bind(ComplexObjects.class));

    ComplexObjects complexObjects = injector.getInstance(ComplexObjects.class);

    InjectedObject complexObjectPropValue = complexObjects.getMyComplexObjectProp().get();
    assertThat(complexObjectPropValue.getName()).isEqualTo("Rush B");
    assertThat(complexObjectPropValue.getAge()).isEqualTo(12);

    List<InjectedObject> listOfComplexObjectPropValue = complexObjects.getMyListOfComplexObjectProp().get();
    assertThat(listOfComplexObjectPropValue).hasSize(2);
    assertThat(listOfComplexObjectPropValue.get(0).getName()).isEqualTo("dustII");
    assertThat(listOfComplexObjectPropValue.get(0).getAge()).isEqualTo(3);
    assertThat(listOfComplexObjectPropValue.get(1).getName()).isEqualTo("inferno");
    assertThat(listOfComplexObjectPropValue.get(1).getAge()).isEqualTo(16);

    String stringPropValue = complexObjects.getMyStringProp().get();
    assertThat(stringPropValue).isEqualTo("awp");
  }

  @Test(expected = FailedToCreatePropException.class)
  public void throws_on_null_values() {
    Injector injector = createInjector(binder -> binder.bind(NullValue.class));

    injector.getInstance(NullValue.class);
  }

  @Test(expected = RequiredNamedAnnotationException.class)
  public void throws_un_unnamed_prop() {
    Injector injector = createInjector(binder -> binder.bind(UnnamedProp.class));

    injector.getInstance(UnnamedProp.class);
  }

  @Test
  public void can_have_multiple_objects_using_the_same_prop() {
    Injector injector = createInjector(binder -> {
      binder.bind(ComplexObjects.class);
      binder.bind(SamePropertyAsComplexObjects.class);
    });

    ComplexObjects complexObjects = injector.getInstance(ComplexObjects.class);
    SamePropertyAsComplexObjects samePropertyAsComplexObjects = injector.getInstance(SamePropertyAsComplexObjects
                                                                                       .class);

    assertThat(complexObjects.getMyComplexObjectProp()).isSameAs(samePropertyAsComplexObjects.getMyComplexObjectProp());
  }

  @Test(expected = IllegalStateException.class)
  public void cannot_use_twice_same_prop_in_same_object() {
    Injector injector = createInjector(binder -> binder.bind(UsesTwiceSameProp.class));

    injector.getInstance(UsesTwiceSameProp.class);
  }

  @Test
  public void can_use_both_named_annotations() {
    Injector injector = createInjector(binder -> binder.bind(BothNamedAnnotations.class));

    BothNamedAnnotations bothNamedAnnotations = injector.getInstance(BothNamedAnnotations.class);

    assertThat(bothNamedAnnotations.getStringProp1().get()).isEqualTo("awp");
    assertThat(bothNamedAnnotations.getStringProp2().get()).isEqualTo("usp");
  }

  @Test(expected = FailedToCreatePropException.class)
  public void throws_if_deserialization_fails() {
    Injector injector = createInjector(binder -> binder.bind(InvalidJSON.class));

    injector.getInstance(InvalidJSON.class);
  }

  @Test
  public void module_with_no_element_does_not_throw() {
    createInjector(binder -> {
    });
  }

  @Test
  public void module_with_no_binding_does_not_throw() {
    createInjector(Binder::requireExplicitBindings);
  }

  @Test
  public void module_with_provider() {
    Injector injector = createInjector(binder -> binder.bind(MyInterface.class).toProvider(MyInterfaceProvider.class));

    MyInterface myInterface = injector.getInstance(MyInterface.class);

    assertThat(myInterface.getPropValue()).isEqualTo("awp");
  }

  @Test(expected = RequiredNamedAnnotationException.class)
  public void module_with_empty_named_annotation() {
    createInjector(binder -> binder.bind(EmptyNamedAnnotation.class));
  }

  @Test
  public void private_module_support() {
    Injector injector = createInjector(new PrivateModule() {
      @Override
      protected void configure() {
        bind(ComplexObjects.class);
        expose(ComplexObjects.class);
      }
    });

    injector.getInstance(ComplexObjects.class);
  }

  @Test
  public void inline_provider_support() {
    Injector injector = createInjector(new InlineProviderModule());

    injector.getInstance(InjectedObject.class);
  }

  private Injector createInjector(Module... modules) {
    AutoPropModule autoPropModule = new AutoPropModule("io.pleo",
                                                       Arrays.asList(modules),
                                                       new ArchaiusPropFactory(),
                                                       new JacksonParserFactory());
    List<Module> allModules = new ArrayList<>();
    allModules.add(autoPropModule);
    allModules.addAll(Arrays.asList(modules));
    try {
      return Guice.createInjector(allModules);
    } catch (CreationException ex) {
      throw (RuntimeException) ex.getCause();
    }
  }
}
