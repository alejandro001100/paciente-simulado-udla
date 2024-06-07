package com.pacientesimulado.application.views.registro;

import com.pacientesimulado.application.data.Actor;
import com.pacientesimulado.application.data.Usuario;
import com.pacientesimulado.application.services.ActorService;
import com.pacientesimulado.application.services.UsuarioService;
import com.pacientesimulado.application.views.login.LoginView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Registro")
@Route(value = "registro")
public class RegistroView extends VerticalLayout {

    private final Binder<Usuario> usuarioBinder = new Binder<>(Usuario.class);
    private final Binder<Actor> actorBinder = new Binder<>(Actor.class);

    public RegistroView(UsuarioService usuarioService, ActorService actorService) {
        setWidth("100%");
        getStyle().set("flex-grow", "1");
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(FlexComponent.Alignment.CENTER);

        H3 title = new H3("Regístrate");
        H6 subtitle = new H6("Coloca tus datos para comenzar el registro.");
        subtitle.getStyle().set("text-align", "center");

        TextField nombreField = new TextField("Nombre");
        nombreField.setWidthFull();

        TextField apellidoField = new TextField("Apellido");
        apellidoField.setWidthFull();

        EmailField emailField = new EmailField("Correo electrónico");
        emailField.setWidthFull();
        emailField.setErrorMessage("Por favor, ingrese un correo válido.");
        emailField.setPattern("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

        PasswordField passwordField = new PasswordField("Contraseña");
        passwordField.setWidthFull();

        Select<String> roleSelect = new Select<>();
        roleSelect.setItems("Actor", "Doctor");
        roleSelect.setLabel("¿Eres?");
        roleSelect.setWidthFull();

        NumberField edadField = new NumberField("Edad");
        edadField.setWidthFull();
        edadField.setVisible(false);

        ComboBox <String> sexo = new ComboBox<>("Sexo");
        sexo.setItems("Femenino", "Masculino");
        sexo.setWidthFull();
        sexo.setVisible(false);

        NumberField pesoField = new NumberField("Peso");
        pesoField.setWidthFull();
        pesoField.setVisible(false);

        NumberField alturaField = new NumberField("Altura");
        alturaField.setWidthFull();
        alturaField.setVisible(false);

        roleSelect.addValueChangeListener(event -> {
            boolean isActor = "Actor".equals(event.getValue());
            edadField.setVisible(isActor);
            sexo.setVisible(isActor);
            pesoField.setVisible(isActor);
            alturaField.setVisible(isActor);
        });

        Button registerButton = new Button("Registrar");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(event -> {
            try {
                if (roleSelect.getValue() == null) {
                    Notification.show("Por favor, seleccione un rol.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                Usuario usuario = new Usuario();
                usuarioBinder.writeBean(usuario);

                if (usuarioService.obtenerUsuarioPorCorreo(usuario.getCorreo()).isPresent()) {
                    Notification.show("Correo electrónico ya registrado.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                usuarioService.registrarUsuario(usuario);

                if ("Actor".equals(usuario.getRol())) {
                    Actor actor = new Actor();
                    actorBinder.writeBean(actor);
                    actor.setCorreo(usuario.getCorreo());
                    actorService.guardarActor(actor);
                }

                Notification.show("Registro exitoso!", 3000, Notification.Position.MIDDLE);
                getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            } catch (ValidationException e) {
                Notification.show("Por favor, complete todos los campos correctamente.", 3000, Notification.Position.MIDDLE);
            }
        });

        usuarioBinder.forField(nombreField).asRequired("Nombre es requerido").bind(Usuario::getNombre, Usuario::setNombre);
        usuarioBinder.forField(apellidoField).asRequired("Apellido es requerido").bind(Usuario::getApellido, Usuario::setApellido);
        usuarioBinder.forField(emailField)
                .withValidator(new EmailValidator("Correo inválido"))
                .asRequired("Correo es requerido")
                .bind(Usuario::getCorreo, Usuario::setCorreo);
        usuarioBinder.forField(passwordField).asRequired("Contraseña es requerida").bind(Usuario::getContraseña, Usuario::setContraseña);
        usuarioBinder.forField(roleSelect).asRequired("Rol es requerido").bind(Usuario::getRol, Usuario::setRol);

        actorBinder.forField(edadField)
                .withConverter(new DoubleToIntegerConverter())
                .withValidator(p -> p == null || (p >= 5 && p <= 90), "La edad debe estar entre 5 y 90 años")
                .bind(Actor::getEdad, Actor::setEdad);
        actorBinder.forField(sexo)
                .asRequired("Sexo es requerido")
                .bind(Actor::getSexo, Actor::setSexo);
        actorBinder.forField(pesoField)
                .withValidator(p -> p == null || (p >= 30 && p <= 300), "Peso debe estar entre 30 kg y 300 kg")
                .bind(Actor::getPeso, Actor::setPeso);
        actorBinder.forField(alturaField)
                .withValidator(a -> a == null || (a >= 1.0 && a <= 2.50), "Altura debe estar entre 1.0 m y 2.50 m")
                .bind(Actor::getAltura, Actor::setAltura);
        Button backButton = new Button("Volver al Login");
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
        add(backButton);

        add(title, subtitle, nombreField, apellidoField, emailField, passwordField, roleSelect, edadField, sexo, pesoField, alturaField, registerButton, backButton);
    }

    private static class DoubleToIntegerConverter implements Converter<Double, Integer> {
        @Override
        public Result<Integer> convertToModel(Double value, ValueContext context) {
            return value == null ? Result.ok(null) : Result.ok(value.intValue());
        }

        @Override
        public Double convertToPresentation(Integer value, ValueContext context) {
            return value == null ? null : value.doubleValue();
        }
    }
}
