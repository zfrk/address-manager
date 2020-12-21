package com.sap.cloud.sdk.examples.addressmgr;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import com.google.gson.Gson;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.datamodel.odata.helper.Order;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerAddress;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.control.Try;

@WebServlet("/api/business-partners")
public class BusinessPartnerServlet extends HttpServlet {
    /**
     *
     */

    private static final long serialVersionUID = 1L;
    private static final String CATEGORY_PERSON = "1";

    private static final Logger logger = LoggerFactory.getLogger(BusinessPartnerServlet.class);

    @Override
    public void init() throws ServletException{

        DestinationAccessor.tryGetDestination("E4Q");

    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final HttpDestination httpDestination = DestinationAccessor.getDestination("E4Q").asHttp();
        final String jsonResult ;
        final String id = request.getParameter("id");


        try {

            if( id == null){

                logger.info("Retrieving all business partners");

                final List<BusinessPartner> result  = new DefaultBusinessPartnerService().getAllBusinessPartner()
                        .select(BusinessPartner.BUSINESS_PARTNER, BusinessPartner.LAST_NAME, BusinessPartner.FIRST_NAME)
                        .filter(BusinessPartner.BUSINESS_PARTNER_CATEGORY.eq(CATEGORY_PERSON))
                        .orderBy(BusinessPartner.LAST_NAME, Order.ASC).execute(httpDestination);

                        jsonResult = new Gson().toJson(result);
            }else{

                logger.info("Retrieving business partners with id");

                final BusinessPartner result = new DefaultBusinessPartnerService()
                    .getBusinessPartnerByKey(id)
                    .select(BusinessPartner.BUSINESS_PARTNER,
                        BusinessPartner.FIRST_NAME,
                        BusinessPartner.LAST_NAME,
                        BusinessPartner.IS_MALE,
                        BusinessPartner.IS_FEMALE,
                        BusinessPartner.CREATION_DATE,
                        BusinessPartner.TO_BUSINESS_PARTNER_ADDRESS.select(BusinessPartnerAddress.BUSINESS_PARTNER,
                            BusinessPartnerAddress.ADDRESS_ID,
                            BusinessPartnerAddress.COUNTRY,
                            BusinessPartnerAddress.POSTAL_CODE, 
                            BusinessPartnerAddress.CITY_NAME, 
                            BusinessPartnerAddress.STREET_NAME,
                            BusinessPartnerAddress.HOUSE_NUMBER 
                            )
                        )
                    .execute(httpDestination) ;

                    jsonResult = new Gson().toJson(result);

            }


        } catch (final ODataException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
            return;
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResult);
                        


    }
}