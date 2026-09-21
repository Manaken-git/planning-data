package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Eleve;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class EleveMapperTest {

    private final EleveMapper mapper = Mappers.getMapper(EleveMapper.class);

    @Test
    void shouldMapEleveToEleveDTOWithClasseId() {
        Classe classe = new Classe();
        classe.setId(42L);
        classe.setNom("BTS SIO");

        Eleve eleve = new Eleve();
        eleve.setId(1L);
        eleve.setNom("Martin");
        eleve.setPrenom("Alice");
        eleve.setClasse(classe);

        EleveDTO dto = mapper.toDto(eleve);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.nom()).isEqualTo("Martin");
        assertThat(dto.prenom()).isEqualTo("Alice");
        assertThat(dto.classeId()).isEqualTo(42L);
    }

    @Test
    void shouldMapEleveWithoutClasse() {
        Eleve eleve = new Eleve();
        eleve.setId(2L);
        eleve.setNom("Durand");
        eleve.setPrenom("Bob");

        EleveDTO dto = mapper.toDto(eleve);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.nom()).isEqualTo("Durand");
        assertThat(dto.prenom()).isEqualTo("Bob");
        assertThat(dto.classeId()).isNull();
    }
}
