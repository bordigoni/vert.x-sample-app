import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {Client} from './client.model';

const baseUrl = "http://localhost:8080/client";

@Injectable()
export class ClientService {


  constructor(private http: Http) {
  }

  getAll(): Observable<Client[]> {
    return this.http.get(baseUrl).map(r => r.json());
  }


  get(id: string): Observable<Client> {
    return this.http.get(baseUrl + "/" + id).map(r => r.json());
  }

  save(client: Client): Observable<any> {
    return this.http.post(baseUrl, client);
  }

  update(client: Client): Observable<any> {
    return this.http.put(baseUrl + "/" + client.id, client);
  }
}
