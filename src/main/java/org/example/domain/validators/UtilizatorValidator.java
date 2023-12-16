package org.example.domain.validators;

import org.example.domain.Utilizator;

public class UtilizatorValidator implements Validator<Utilizator> {
    @Override
    public void validate(Utilizator entity) throws ValidationException
    {
        if(entity.getFirstName().length()<3) throw new ValidationException("Prenumele trebuie sa aiba cel putin 3 caractere.");
        if(entity.getLastName().length()<3) throw new ValidationException("Numele trebuie sa aiba cel putin 3 caractere.");
    }
}

