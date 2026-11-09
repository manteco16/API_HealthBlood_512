/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;

import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lasalle.controller.ControllerrUsuario;
import org.lasalle.model.Usuario;

/**
 *
 * @author osmar
 */
@Path("usuario")
public class RestUsuario {

    @Path("saveUsuario")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveUsuario(
            @FormParam("nombre") String nombre,
            @FormParam("primerApellido") String primerApellido,
            @FormParam("segundoApellido") String segundoApellido,
            @FormParam("email") String email) {

        Gson gson = new Gson();

        try {
            if (nombre == null || nombre.isBlank()
                    || email == null || email.isBlank()) {

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"result\":\"nombre y email son obligatorios\"}")
                        .build();
            }

            Usuario user = new Usuario();
            user.setNombre(nombre);
            user.setPrimerApellido(primerApellido);
            user.setSegundoApellido(segundoApellido);
            user.setEmail(email);

            ControllerrUsuario cu = new ControllerrUsuario();
            int idGenerado = cu.saveUsuario(user);

            if (idGenerado <= 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"result\":\"No se pudo guardar el usuario\"}")
                        .build();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(gson.toJson(user))
                    .build();

        } catch (Exception e) {

            e.printStackTrace();

            String error = """
                {"result": "Error al guardar el usuario"}
                """;

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
}
