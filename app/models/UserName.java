package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserName extends Model {

    @Id
    private Long id;
    private String name;

    // Adicionando um Finder para o modelo UserName
    public static final Finder<Long, UserName> find = new Finder<>(UserName.class);

    // getters e setters (ou pode usar Lombok para gerar automaticamente)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
