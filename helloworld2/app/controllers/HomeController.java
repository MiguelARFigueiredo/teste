package controllers;

import models.UserName;
import play.mvc.*;
import play.data.Form;
import play.data.FormFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Controlador responsável por lidar com as requisições relacionadas à aplicação.
 */
public class HomeController extends Controller {

    private final FormFactory formFactory;

    @Inject
    public HomeController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Método para renderizar a página inicial.
     *
     * @return Result Resultado da renderização da página inicial.
     */
    public Result index() {
        // Procura todos os utilizadores na base de dados
        List<UserName> users = UserName.find.all();

        // Cria um formulário vazio para a classe UserName
        Form<UserName> nameForm = formFactory.form(UserName.class);

        // Mensagem de sucesso (obtida da flash scope)
        String message = flash().get("success");

        // Renderiza a view passando o formulário, a mensagem e a lista de utilizadores
        return ok(views.html.events.render(nameForm, message, users));
    }

    /**
     * Método para renderizar a página de exploração.
     *
     * @return Result Resultado da renderização da página de exploração.
     */
    public Result explore() {
        return ok(views.html.explore.render());
    }

    /**
     * Método para renderizar a página de tutorial.
     *
     * @return Result Resultado da renderização da página de tutorial.
     */
    public Result tutorial() {
        return ok(views.html.tutorial.render());
    }

    /**
     * Método para renderizar a página WildSmile para o utilizador "miguel".
     *
     * @return Result Resultado da renderização da página WildSmile.
     */
    public Result wildsmile() {
        return ok(views.html.wildsmile.render());
    }

    /**
     * Método para renderizar a página de eventos com um formulário vazio.
     *
     * @return Result Resultado da renderização da página de eventos.
     */
    public Result events() {
        // Cria um formulário vazio para a classe UserName
        Form<UserName> nameForm = formFactory.form(UserName.class);

        // Renderiza a página de eventos passando o formulário vazio
        return ok(views.html.events.render(nameForm, "", null));
    }

    /**
     * Método para lidar com a submissão do formulário de nome.
     *
     * @return Result Resultado com base na validação do formulário:
     *                - badRequest: Se houver erros de validação, renderiza a página inicial com os erros.
     *                - ok: Se o formulário for válido, salva os dados e redireciona para a página inicial.
     */
    public Result submit() {
        // Obtém o formulário submetido da requisição
        Form<UserName> nameForm = formFactory.form(UserName.class).bindFromRequest();

        if (nameForm.hasErrors()) {
            // Retorna a mesma página com os erros de validação
            return badRequest(views.html.events.render(nameForm, "", null));
        } else {
            // Guarda os dados do formulário
            UserName formData = nameForm.get();
            formData.save();

            // Define uma mensagem de sucesso na flash scope
            flash("success", "Nome enviado com sucesso!");

            // Redireciona de volta para a página inicial
            return redirect(routes.HomeController.index());
        }
    }
}
