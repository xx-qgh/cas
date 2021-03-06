package org.apereo.cas.config;

import com.google.common.collect.Lists;
import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsConfig;
import org.apereo.cas.support.spnego.authentication.handler.support.JcifsSpnegoAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.handler.support.NtlmAuthenticationHandler;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoPrincipalResolver;
import org.apereo.cas.support.spnego.web.flow.SpnegoCredentialsAction;
import org.apereo.cas.support.spnego.web.flow.SpnegoNegociateCredentialsAction;
import org.apereo.cas.support.spnego.web.flow.client.BaseSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.support.spnego.web.flow.client.HostNameSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.support.spnego.web.flow.client.LdapSpnegoKnownClientSystemsFilterAction;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link SpnegoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SpnegoConfiguration {

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired(required = false)
    @Qualifier("spnegoClientActionConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired(required = false)
    @Qualifier("spnegoClientActionSearchRequest")
    private SearchRequest searchRequest;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public Authentication spnegoAuthentication() {
        return new Authentication();
    }


    @Bean
    @RefreshScope
    public JcifsConfig jcifsConfig() {
        final JcifsConfig c = new JcifsConfig();

        c.setJcifsDomain(casProperties.getAuthn().getSpnego().getJcifsDomain());
        c.setJcifsDomainController(casProperties.getAuthn().getSpnego().getJcifsDomainController());
        c.setJcifsNetbiosCachePolicy(casProperties.getAuthn().getSpnego().getCachePolicy());
        c.setJcifsNetbiosWins(casProperties.getAuthn().getSpnego().getJcifsNetbiosWins());
        c.setJcifsPassword(casProperties.getAuthn().getSpnego().getJcifsPassword());
        c.setJcifsServicePassword(casProperties.getAuthn().getSpnego().getJcifsServicePassword());
        c.setJcifsServicePrincipal(casProperties.getAuthn().getSpnego().getJcifsServicePrincipal());
        c.setJcifsSocketTimeout(casProperties.getAuthn().getSpnego().getTimeout());
        c.setJcifsUsername(casProperties.getAuthn().getSpnego().getJcifsUsername());
        c.setKerberosConf(casProperties.getAuthn().getSpnego().getKerberosConf());
        c.setKerberosDebug(casProperties.getAuthn().getSpnego().getKerberosDebug());
        c.setKerberosKdc(casProperties.getAuthn().getSpnego().getKerberosKdc());
        c.setKerberosRealm(casProperties.getAuthn().getSpnego().getKerberosRealm());
        c.setLoginConf(casProperties.getAuthn().getSpnego().getLoginConf());
        c.setUseSubjectCredsOnly(casProperties.getAuthn().getSpnego().isUseSubjectCredsOnly());

        return c;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler spnegoHandler() {
        final JcifsSpnegoAuthenticationHandler h = new JcifsSpnegoAuthenticationHandler();
        h.setPrincipalFactory(spnegoPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setAuthentication(spnegoAuthentication());
        h.setPrincipalWithDomainName(casProperties.getAuthn().getSpnego().isPrincipalWithDomainName());
        h.setNTLMallowed(casProperties.getAuthn().getSpnego().isNtlmAllowed());
        return h;
    }


    @Bean
    @RefreshScope
    public AuthenticationHandler ntlmAuthenticationHandler() {
        final NtlmAuthenticationHandler ntlm = new NtlmAuthenticationHandler();
        ntlm.setDomainController(casProperties.getAuthn().getNtlm().getDomainController());
        ntlm.setIncludePattern(casProperties.getAuthn().getNtlm().getIncludePattern());
        ntlm.setLoadBalance(casProperties.getAuthn().getNtlm().isLoadBalance());
        ntlm.setPrincipalFactory(ntlmPrincipalFactory());
        ntlm.setServicesManager(servicesManager);
        return ntlm;
    }

    @Bean
    public PrincipalFactory ntlmPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver spnegoPrincipalResolver() {
        final SpnegoPrincipalResolver r = new SpnegoPrincipalResolver();
        r.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getSpnego().getPrincipalTransformation()));
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getSpnego().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getSpnego().getPrincipal().isReturnNull());
        r.setPrincipalFactory(spnegoPrincipalFactory());
        return r;
    }

    @Bean
    public PrincipalFactory spnegoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public SpnegoCredentialsAction spnego() {
        final SpnegoCredentialsAction a = new SpnegoCredentialsAction();
        a.setNtlm(casProperties.getAuthn().getSpnego().isNtlm());
        a.setSend401OnAuthenticationFailure(casProperties.getAuthn().getSpnego().isSend401OnAuthenticationFailure());
        return a;
    }

    @Bean
    @RefreshScope
    public Action negociateSpnego() {
        final SpnegoNegociateCredentialsAction a =
                new SpnegoNegociateCredentialsAction();
        a.setMixedModeAuthentication(casProperties.getAuthn().getSpnego().isMixedModeAuthentication());
        a.setNtlm(casProperties.getAuthn().getSpnego().isNtlm());
        a.setSupportedBrowsers(Lists.newArrayList(casProperties.getAuthn().getSpnego().getSupportedBrowsers()));
        return a;
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        final BaseSpnegoKnownClientSystemsFilterAction a =
                new BaseSpnegoKnownClientSystemsFilterAction();

        a.setIpsToCheckPattern(casProperties.getAuthn().getSpnego().getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(casProperties.getAuthn().getSpnego().getAlternativeRemoteHostAttribute());
        a.setTimeout(casProperties.getAuthn().getSpnego().getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        final HostNameSpnegoKnownClientSystemsFilterAction a =
                new HostNameSpnegoKnownClientSystemsFilterAction(casProperties.getAuthn().getSpnego().getHostNamePatternString());
        a.setIpsToCheckPattern(casProperties.getAuthn().getSpnego().getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(casProperties.getAuthn().getSpnego().getAlternativeRemoteHostAttribute());
        a.setTimeout(casProperties.getAuthn().getSpnego().getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        final LdapSpnegoKnownClientSystemsFilterAction l =
                new LdapSpnegoKnownClientSystemsFilterAction(this.connectionFactory,
                        this.searchRequest, casProperties.getAuthn().getSpnego().getSpnegoAttributeName());

        l.setIpsToCheckPattern(casProperties.getAuthn().getSpnego().getIpsToCheckPattern());
        l.setAlternativeRemoteHostAttribute(casProperties.getAuthn().getSpnego().getAlternativeRemoteHostAttribute());
        l.setTimeout(casProperties.getAuthn().getSpnego().getDnsTimeout());
        return l;
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        authenticationHandlersResolvers.put(spnegoHandler(), spnegoPrincipalResolver());
    }
}
