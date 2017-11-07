import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {PollSource} from './pollsource.model';

import 'rxjs/add/operator/map'

const baseUrl = "http://localhost:8080/client";

@Injectable()
export class PollsourceService {

  constructor(private http: Http) {
  }

  save(clientId: string, pollSource: PollSource): Observable<PollSource> {
    return this.http.post(formatUrl(clientId), pollSource).map(r => r.json());
  }

  update(clientId: string, pollSource: PollSource): Observable<any> {
    return this.http.put(formatUrl(clientId, pollSource.id), pollSource);
  }

  getAll(clientId: string): Observable<Array<PollSource>> {
    return this.http.get(formatUrl(clientId))
      .map(r => r.json());
  }

  get(clientId: string, id: string): Observable<PollSource> {
    return this.http.get(formatUrl(clientId, id))
      .map(r => r.json());
  }

  remove(clientId: string, id: string): Observable<any> {
    return this.http.delete(formatUrl(clientId, id));
  }

}

const formatUrl = (clientId: string, id?: string) => baseUrl + '/' + clientId + '/pollsource' + (id ? '/' + id : '');
