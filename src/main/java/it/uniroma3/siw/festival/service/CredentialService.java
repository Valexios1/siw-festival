package it.uniroma3.siw.festival.service;


import it.uniroma3.siw.festival.model.Credentials;
import it.uniroma3.siw.festival.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CredentialService {


    @Autowired
    private CredentialRepository credentialRepository;

    public Credentials getCredentials(Long id){
        // getReferenceById e' il metodo previsto da JpaRepository al posto di findById(id).get():
        // restituisce un riferimento all'entita' senza caricarla finche' non viene usata.
        return credentialRepository.getReferenceById(id);
    }

    public Credentials getCredentials(String username){
        return credentialRepository.findByUsername(username).orElse(null);
    }

    public Credentials saveCredentials(Credentials credentials){
        return credentialRepository.save(credentials);
    }

}
