// HomeController

package controllers;

import views.html.events;
import models.RevenueDates;
import models.UserName;
import play.mvc.*;
import play.data.Form;
import play.data.FormFactory;
import javax.inject.Inject;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

// Importe a classe PaymentReport aqui
import models.PaymentReport;
import java.util.Arrays;

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
     * Método para renderizar a página inicial da versão 2.
     *
     * @return Result Resultado da renderização da página inicial da versão 2.
     */
    public Result indexv2() {
        return ok(views.html.index.render());
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
     * - badRequest: Se houver erros de validação, renderiza a página inicial com os erros.
     * - ok: Se o formulário for válido, salva os dados e redireciona para a página inicial.
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

            // Passa o nome do usuário inserido de volta para a página de eventos
            return ok(views.html.events.render(formFactory.form(UserName.class), "Nome enviado com sucesso!", Arrays.asList(formData)));
        }
    }

    /**
     * Método para iniciar o processo de obtenção de receitas da EasyPay.
     *
     * Este método chama diretamente o método existente getCorrectRevenuefromEasypay(startDate, endDate)
     * da classe PaymentReport com as datas específicas para a consulta.
     */
    public Result startGetCorrectRevenueProcess() {
        Http.Context ctx = Http.Context.current();
        Http.Request request = ctx.request();

        Form<RevenueDates> revenueForm = formFactory.form(RevenueDates.class).bindFromRequest(request);
        if (revenueForm.hasErrors()) {
            return badRequest("Erro ao processar as datas fornecidas.");
        } else {
            RevenueDates dates = revenueForm.get();
            String startDateString = dates.getStartDate();
            String endDateString = dates.getEndDate();

            // Converte as strings de data para LocalDate
            LocalDate startDate = LocalDate.parse(startDateString);
            LocalDate endDate = LocalDate.parse(endDateString);

            // Chama o método da classe PaymentReport para iniciar o processo de obtenção de receitas
            PaymentReport.getCorrectRevenuefromEasypay(startDate, endDate);

            // Retorna uma mensagem de sucesso para o usuário (ou uma página de confirmação)
            return ok(events.render(formFactory.form(UserName.class), "Processo de obtenção de receitas iniciado para o período de " + startDate + " a " + endDate, null));
        }
    }
}
