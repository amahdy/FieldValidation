package org.vaadin.amahdy.fieldvalidation;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;

public class GenericTextField<T> extends TextField {

  class Content {
    T content;
    public T getContent() {
      return content;
    }
    public void setContent(T content) {
      this.content = content;
    }
  }

  private Content content = new Content();
  private Binder<Content> binder = new Binder<>();
  private Converter<String,T> baseConverter;
  private List<Validator<T>> validators = new ArrayList<>();
  private List<Converter<T,?>> converters = new ArrayList<>();

  private void init(Converter<String,T> baseConverter) {
    this.baseConverter = baseConverter;
    binder.setBean(content);
  }

  /**
   * This is the default consutrctor
   * It works only when the TextField has `String` values.
   */
  public GenericTextField() {
    init(new Converter<String, T>() {
      @Override
      public Result<T> convertToModel(
        String fieldValue, ValueContext context) {
        return Result.ok((T)fieldValue);
      }
      @Override
      public String convertToPresentation(
        T typeValue, ValueContext context) {
        return typeValue==null ? "" : String.valueOf(typeValue);
      }
    });
  }

  /**
   * Sopecify how to convert from the provided `T` to `String`.
   */
  public GenericTextField(Converter<String,T> baseConverter) {
    init(baseConverter);
  }

  /**
   * Sopecify how to convert from the provided `T` to `String`.
   */
  public GenericTextField(
    SerializableFunction<String, T> toModel,
    SerializableFunction<T, String> toPresentation,
    String errorMessage) {
    this(
      Converter.from(
        toModel
        , toPresentation
        , exception -> errorMessage)
    );
  }

  public void addValidator(
    SerializablePredicate<T> predicate,
    String errorMessage) {
      addValidator(Validator.from(predicate, errorMessage));
  }

  public void addValidator(Validator<T> validator) {
    validators.add(validator);
    build();
  }

  public <NEWTARGET> void addConverter (
    SerializableFunction<T, NEWTARGET> toModel,
    SerializableFunction<NEWTARGET, T> toPresentation,
    String errorMessage) {
    addConverter(
      Converter.from(
        toModel
        , toPresentation
        , exception -> errorMessage)
    );
  }

  public void addConverter(Converter<T, ?> converter) {
    converters.add(converter);
    build();
  }

  private void build() {
    Content bean = binder.getBean();
    BindingBuilder<Content, T> builder =
      binder.forField(this)
      .withConverter(baseConverter);

    for(Converter<T, ?> c: converters) {
      builder.withConverter(c);
    }

    for(Validator<T> v: validators) {
      builder.withValidator(v);
    }

    builder.bind(
      Content::getContent, Content::setContent);

    binder.setBean(bean);
  }
}
