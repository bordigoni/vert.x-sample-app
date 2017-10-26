import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {Client} from './client.model';

const baseUrl = "http://localhost:8080/client";

@Injectable()
export class ClientService {


  clients: Client[] = [{id: "1", name: "Bordigoni", email: "benoit@bordigoni.fr", password: "secret"}];

  constructor(private http: Http) {
  }

  getAll(): Observable<Client[]> {
    return this.http.get(baseUrl).map(r => r.json());

    // return Observable.create(obs => {
    //   obs.next(this.clients);
    //   obs.complete();
    // });

  }


  get(id: string): Observable<Client> {
    return this.http.get(baseUrl + "/" + id).map(r => r.json());
    // return Observable.create(obs => {
    //   obs.next(this.clients.filter(client => client.id == id)[0]);
    //   obs.complete();
    // });
  }

  save(client: Client): Observable<Client> {
    return this.http.post(baseUrl, client);
    // const current = this.clients.filter(e => e.id == client.id)[0];
    // if (current) {
    //   current.name = client.name;
    //   current.email = client.email;
    //   current.password = client.password;
    // } else {
    //   client.id = client.email;
    //   this.clients.push(client);
    // }
  }
}
